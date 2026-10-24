package com.practica.webAvanzada.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.practica.webAvanzada.dto.CatalogoHabilidadesResponse;
import com.practica.webAvanzada.dto.PerfilProfesionalResponse;
import com.practica.webAvanzada.dto.RecomendacionProyectosResponse;
import com.practica.webAvanzada.dto.RecomendacionProyectosResponse.ProyectoRecomendado;
import com.practica.webAvanzada.dto.SugerenciaHabilidadesResponse;
import com.practica.webAvanzada.exceptions.ReglaNegocioException;
import com.practica.webAvanzada.exceptions.ServicioNoDisponibleException;
import com.practica.webAvanzada.models.Alumno;
import com.practica.webAvanzada.models.ProyectoResidencia;
import com.practica.webAvanzada.models.enums.EstadoProyecto;
import com.practica.webAvanzada.repositories.PostulacionRepository;
import com.practica.webAvanzada.repositories.ProyectoResidenciaRepository;
import com.practica.webAvanzada.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// Consume el microservicio de recomendaciones (FastAPI). Este no conoce la base de datos:
// en cada petición se le envían las habilidades del alumno y los proyectos a evaluar
@Slf4j
@Service
@RequiredArgsConstructor
public class RecomendacionService {

    // Límites que acepta el microservicio por petición
    private static final int MAXIMO_HABILIDADES = 100;
    private static final int MAXIMO_PROYECTOS = 500;

    private final RestClient recomendacionesRestClient;
    private final AlumnoService alumnoService;
    private final ProyectoResidenciaService proyectoService;
    private final ProyectoResidenciaRepository proyectoRepository;
    private final PostulacionRepository postulacionRepository;

    @Transactional(readOnly = true)
    public RecomendacionProyectosResponse recomendarProyectos(UsuarioActual actual, int limite) {
        Alumno alumno = alumnoService.buscarPorUsuario(actual.idUsuario());
        List<String> habilidades = habilidadesDe(alumno);
        Set<Long> yaPostulados = postulacionRepository.findByAlumnoNumeroControl(alumno.getNumeroControl()).stream()
                .map(p -> p.getProyecto().getIdProyecto())
                .collect(Collectors.toSet());
        Map<Long, ProyectoResidencia> candidatos = new LinkedHashMap<>();
        proyectoRepository.findByEstado(EstadoProyecto.DISPONIBLE,
                        PageRequest.of(0, MAXIMO_PROYECTOS, Sort.by(Sort.Direction.DESC, "fechaPublicacion")))
                .stream()
                .filter(p -> !yaPostulados.contains(p.getIdProyecto()))
                .forEach(p -> candidatos.put(p.getIdProyecto(), p));
        if (candidatos.isEmpty()) {
            return new RecomendacionProyectosResponse(List.of(), List.of(), List.of());
        }

        List<ProyectoEntrada> proyectos = candidatos.values().stream()
                .map(p -> new ProyectoEntrada(p.getIdProyecto(), p.getTitulo(), separar(p.getTecnologiasRequeridas())))
                .toList();
        RespuestaProyectos respuesta = llamar(() -> recomendacionesRestClient.post()
                .uri("/api/v1/recomendaciones/proyectos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SolicitudProyectos(habilidades, proyectos, Math.min(limite, candidatos.size())))
                .retrieve()
                .body(RespuestaProyectos.class));

        List<ProyectoRecomendado> recomendados = respuesta.resultados().stream()
                .filter(r -> candidatos.containsKey(r.id()))
                .map(r -> new ProyectoRecomendado(proyectoService.aResponse(candidatos.get(r.id())), r.afinidad(),
                        r.habilidadesCoincidentes(), r.habilidadesFaltantes()))
                .toList();
        return new RecomendacionProyectosResponse(respuesta.habilidadesReconocidas(), respuesta.habilidadesNoReconocidas(), recomendados);
    }

    @Transactional(readOnly = true)
    public SugerenciaHabilidadesResponse sugerirHabilidades(UsuarioActual actual, int limite) {
        List<String> habilidades = habilidadesDe(alumnoService.buscarPorUsuario(actual.idUsuario()));
        RespuestaHabilidades respuesta = llamar(() -> recomendacionesRestClient.post()
                .uri("/api/v1/recomendaciones/habilidades")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SolicitudHabilidades(habilidades, limite))
                .retrieve()
                .body(RespuestaHabilidades.class));
        return new SugerenciaHabilidadesResponse(respuesta.habilidadesReconocidas(), respuesta.habilidadesNoReconocidas(),
                respuesta.sugerencias());
    }

    @Transactional(readOnly = true)
    public PerfilProfesionalResponse obtenerPerfil(UsuarioActual actual) {
        List<String> habilidades = habilidadesDe(alumnoService.buscarPorUsuario(actual.idUsuario()));
        RespuestaPerfil respuesta = llamar(() -> recomendacionesRestClient.post()
                .uri("/api/v1/recomendaciones/perfil")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SolicitudPerfil(habilidades))
                .retrieve()
                .body(RespuestaPerfil.class));
        return new PerfilProfesionalResponse(respuesta.habilidadesReconocidas(), respuesta.habilidadesNoReconocidas(),
                respuesta.perfil(), respuesta.habilidadesClave(), respuesta.habilidadesClaveFaltantes());
    }

    public CatalogoHabilidadesResponse obtenerCatalogo(String categoria, String busqueda) {
        return llamar(() -> recomendacionesRestClient.get()
                .uri(uri -> uri.path("/api/v1/habilidades")
                        .queryParamIfPresent("categoria", Optional.ofNullable(categoria).filter(c -> !c.isBlank()))
                        .queryParamIfPresent("busqueda", Optional.ofNullable(busqueda).filter(b -> !b.isBlank()))
                        .build())
                .retrieve()
                .body(CatalogoHabilidadesResponse.class));
    }

    private List<String> habilidadesDe(Alumno alumno) {
        List<String> habilidades = separar(alumno.getHabilidadesTecnicas());
        if (habilidades.isEmpty()) {
            throw new ReglaNegocioException("Agrega tus habilidades técnicas en tu perfil (separadas por comas) para recibir recomendaciones");
        }
        return habilidades;
    }

    // "Java, Spring Boot; SQL" -> [Java, Spring Boot, SQL]
    private static List<String> separar(String texto) {
        if (texto == null) {
            return List.of();
        }
        return Arrays.stream(texto.split("[,;\\n]"))
                .map(String::trim)
                .filter(h -> !h.isEmpty())
                .distinct()
                .limit(MAXIMO_HABILIDADES)
                .toList();
    }

    private <T> T llamar(Supplier<T> peticion) {
        try {
            T respuesta = peticion.get();
            if (respuesta == null) {
                throw new ServicioNoDisponibleException("El servicio de recomendaciones devolvió una respuesta vacía");
            }
            return respuesta;
        } catch (HttpClientErrorException ex) {
            // 422 con habilidades_no_reconocidas: ninguna habilidad del alumno es técnica según el modelo
            if (ex.getStatusCode().value() == 422 && ex.getResponseBodyAsString().contains("habilidades_no_reconocidas")) {
                throw new ReglaNegocioException("Ninguna de tus habilidades es una habilidad técnica reconocida; revisa las habilidades de tu perfil");
            }
            log.error("El microservicio de recomendaciones rechazó la petición ({}): {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ServicioNoDisponibleException("El servicio de recomendaciones rechazó la petición");
        } catch (RestClientException ex) {
            log.error("No se pudo consultar el microservicio de recomendaciones: {}", ex.getMessage());
            throw new ServicioNoDisponibleException("El servicio de recomendaciones no está disponible, inténtalo más tarde");
        }
    }

    // Contrato del microservicio (usa snake_case)

    private record ProyectoEntrada(Long id, String titulo, List<String> habilidades) {
    }

    private record SolicitudProyectos(List<String> habilidades, List<ProyectoEntrada> proyectos, int limite) {
    }

    private record SolicitudHabilidades(List<String> habilidades, int limite) {
    }

    private record SolicitudPerfil(List<String> habilidades) {
    }

    private record ResultadoProyecto(
            Long id,
            double afinidad,
            @JsonProperty("habilidades_coincidentes") List<String> habilidadesCoincidentes,
            @JsonProperty("habilidades_faltantes") List<String> habilidadesFaltantes
    ) {
    }

    private record RespuestaProyectos(
            @JsonProperty("habilidades_reconocidas") List<String> habilidadesReconocidas,
            @JsonProperty("habilidades_no_reconocidas") List<String> habilidadesNoReconocidas,
            List<ResultadoProyecto> resultados
    ) {
    }

    private record RespuestaHabilidades(
            @JsonProperty("habilidades_reconocidas") List<String> habilidadesReconocidas,
            @JsonProperty("habilidades_no_reconocidas") List<String> habilidadesNoReconocidas,
            List<SugerenciaHabilidadesResponse.HabilidadSugerida> sugerencias
    ) {
    }

    private record RespuestaPerfil(
            @JsonProperty("habilidades_reconocidas") List<String> habilidadesReconocidas,
            @JsonProperty("habilidades_no_reconocidas") List<String> habilidadesNoReconocidas,
            String perfil,
            @JsonProperty("habilidades_clave") List<String> habilidadesClave,
            @JsonProperty("habilidades_clave_faltantes") List<String> habilidadesClaveFaltantes
    ) {
    }
}
