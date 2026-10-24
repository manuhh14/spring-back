package com.practica.webAvanzada.services;

import com.practica.webAvanzada.dto.ProyectoRequest;
import com.practica.webAvanzada.dto.ProyectoResponse;
import com.practica.webAvanzada.exceptions.AccesoDenegadoException;
import com.practica.webAvanzada.exceptions.RecursoNoEncontradoException;
import com.practica.webAvanzada.exceptions.ReglaNegocioException;
import com.practica.webAvanzada.models.Empresa;
import com.practica.webAvanzada.models.ProyectoResidencia;
import com.practica.webAvanzada.models.enums.EstadoPostulacion;
import com.practica.webAvanzada.models.enums.EstadoProyecto;
import com.practica.webAvanzada.repositories.PostulacionRepository;
import com.practica.webAvanzada.repositories.ProyectoResidenciaRepository;
import com.practica.webAvanzada.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProyectoResidenciaService {
    private final ProyectoResidenciaRepository proyectoRepository;
    private final PostulacionRepository postulacionRepository;
    private final EmpresaService empresaService;

    @Transactional(readOnly = true)
    public Page<ProyectoResponse> obtenerTodos(EstadoProyecto estado, Pageable pageable) {
        Page<ProyectoResidencia> proyectos = estado == null
                ? proyectoRepository.findAll(pageable)
                : proyectoRepository.findByEstado(estado, pageable);
        return proyectos.map(this::aResponse);
    }

    @Transactional(readOnly = true)
    public ProyectoResponse obtenerPorId(Long id) {
        return aResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<ProyectoResponse> obtenerDeEmpresaActual(UsuarioActual actual) {
        Empresa empresa = empresaService.buscarPorUsuario(actual.idUsuario());
        return proyectoRepository.findByEmpresaIdEmpresa(empresa.getIdEmpresa()).stream().map(this::aResponse).toList();
    }

    @Transactional
    public ProyectoResponse crear(ProyectoRequest request, UsuarioActual actual) {
        Empresa empresa = empresaService.buscarPorUsuario(actual.idUsuario());
        if (!Boolean.TRUE.equals(empresa.getEstadoVerificacion())) {
            throw new ReglaNegocioException("La empresa debe estar verificada por un administrador para publicar proyectos");
        }
        ProyectoResidencia proyecto = new ProyectoResidencia();
        proyecto.setEmpresa(empresa);
        copiarDatos(request, proyecto);
        proyecto.setEstado(EstadoProyecto.DISPONIBLE);
        proyecto.setFechaPublicacion(LocalDate.now());
        return aResponse(proyectoRepository.save(proyecto));
    }

    @Transactional
    public ProyectoResponse actualizar(Long id, ProyectoRequest request, UsuarioActual actual) {
        ProyectoResidencia proyecto = buscar(id);
        verificarPropietario(proyecto, actual);
        if (proyecto.getEstado() == EstadoProyecto.CERRADO) {
            throw new ReglaNegocioException("No se puede modificar un proyecto cerrado");
        }
        long aceptados = aceptados(proyecto);
        if (request.cupoAlumnos() < aceptados) {
            throw new ReglaNegocioException("El proyecto ya tiene " + aceptados + " alumnos aceptados; el cupo no puede ser menor");
        }
        copiarDatos(request, proyecto);
        proyecto.setEstado(aceptados >= request.cupoAlumnos() ? EstadoProyecto.ASIGNADO : EstadoProyecto.DISPONIBLE);
        return ProyectoResponse.de(proyecto, aceptados);
    }

    // Cierra el proyecto y rechaza las postulaciones que seguían en revisión
    @Transactional
    public ProyectoResponse cerrar(Long id, UsuarioActual actual) {
        ProyectoResidencia proyecto = buscar(id);
        verificarPropietario(proyecto, actual);
        proyecto.setEstado(EstadoProyecto.CERRADO);
        postulacionRepository.findByProyectoIdProyecto(id).stream()
                .filter(p -> p.getEstado() == EstadoPostulacion.EN_REVISION)
                .forEach(p -> {
                    p.setEstado(EstadoPostulacion.RECHAZADO);
                    p.setComentariosEmpresa("Rechazada automáticamente: el proyecto fue cerrado");
                });
        return aResponse(proyecto);
    }

    @Transactional
    public void eliminar(Long id, UsuarioActual actual) {
        ProyectoResidencia proyecto = buscar(id);
        verificarPropietario(proyecto, actual);
        if (postulacionRepository.existsByProyectoIdProyecto(id)) {
            throw new ReglaNegocioException("El proyecto tiene postulaciones; ciérralo en lugar de eliminarlo");
        }
        proyectoRepository.delete(proyecto);
    }

    public ProyectoResidencia buscar(Long id) {
        return proyectoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Proyecto", id));
    }

    public void verificarPropietario(ProyectoResidencia proyecto, UsuarioActual actual) {
        if (!actual.esAdmin() && !proyecto.getEmpresa().getUsuario().getIdUsuario().equals(actual.idUsuario())) {
            throw new AccesoDenegadoException("El proyecto no pertenece a tu empresa");
        }
    }

    private long aceptados(ProyectoResidencia proyecto) {
        return postulacionRepository.countByProyectoIdProyectoAndEstado(proyecto.getIdProyecto(), EstadoPostulacion.ACEPTADO);
    }

    public ProyectoResponse aResponse(ProyectoResidencia proyecto) {
        return ProyectoResponse.de(proyecto, aceptados(proyecto));
    }

    private void copiarDatos(ProyectoRequest request, ProyectoResidencia proyecto) {
        proyecto.setTitulo(request.titulo());
        proyecto.setDescripcion(request.descripcion());
        proyecto.setObjetivo(request.objetivo());
        proyecto.setTecnologiasRequeridas(request.tecnologiasRequeridas());
        proyecto.setCupoAlumnos(request.cupoAlumnos());
    }
}
