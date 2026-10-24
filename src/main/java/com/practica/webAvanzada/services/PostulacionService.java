package com.practica.webAvanzada.services;

import com.practica.webAvanzada.dto.CambioEstadoPostulacionRequest;
import com.practica.webAvanzada.dto.PostulacionRequest;
import com.practica.webAvanzada.dto.PostulacionResponse;
import com.practica.webAvanzada.exceptions.AccesoDenegadoException;
import com.practica.webAvanzada.exceptions.RecursoNoEncontradoException;
import com.practica.webAvanzada.exceptions.ReglaNegocioException;
import com.practica.webAvanzada.models.Alumno;
import com.practica.webAvanzada.models.AsesorAcademico;
import com.practica.webAvanzada.models.Postulacion;
import com.practica.webAvanzada.models.ProyectoResidencia;
import com.practica.webAvanzada.models.enums.*;
import com.practica.webAvanzada.repositories.AsesorAcademicoRepository;
import com.practica.webAvanzada.repositories.ExpedienteDocumentoRepository;
import com.practica.webAvanzada.repositories.PostulacionRepository;
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
public class PostulacionService {
    private final PostulacionRepository postulacionRepository;
    private final AsesorAcademicoRepository asesorRepository;
    private final ExpedienteDocumentoRepository documentoRepository;
    private final AlumnoService alumnoService;
    private final AsesorAcademicoService asesorService;
    private final ProyectoResidenciaService proyectoService;

    @Transactional(readOnly = true)
    public Page<PostulacionResponse> obtenerTodos(Pageable pageable) {
        return postulacionRepository.findAll(pageable).map(PostulacionResponse::de);
    }

    @Transactional(readOnly = true)
    public PostulacionResponse obtenerPorId(Long id, UsuarioActual actual) {
        Postulacion postulacion = buscar(id);
        verificarAccesoLectura(postulacion, actual);
        return PostulacionResponse.de(postulacion);
    }

    @Transactional(readOnly = true)
    public List<PostulacionResponse> obtenerDeAlumnoActual(UsuarioActual actual) {
        Alumno alumno = alumnoService.buscarPorUsuario(actual.idUsuario());
        return aResponses(postulacionRepository.findByAlumnoNumeroControl(alumno.getNumeroControl()));
    }

    @Transactional(readOnly = true)
    public List<PostulacionResponse> obtenerDeAsesorActual(UsuarioActual actual) {
        AsesorAcademico asesor = asesorService.buscarPorUsuario(actual.idUsuario());
        return aResponses(postulacionRepository.findByAsesorIdAsesor(asesor.getIdAsesor()));
    }

    @Transactional(readOnly = true)
    public List<PostulacionResponse> obtenerPorProyecto(Long idProyecto, UsuarioActual actual) {
        proyectoService.verificarPropietario(proyectoService.buscar(idProyecto), actual);
        return aResponses(postulacionRepository.findByProyectoIdProyecto(idProyecto));
    }

    @Transactional
    public PostulacionResponse crear(PostulacionRequest request, UsuarioActual actual) {
        Alumno alumno = alumnoService.buscarPorUsuario(actual.idUsuario());
        if (alumno.getEstadoResidencia() != EstadoResidencia.PENDIENTE) {
            throw new ReglaNegocioException("Ya tienes una residencia en curso o finalizada");
        }
        ProyectoResidencia proyecto = proyectoService.buscar(request.idProyecto());
        if (proyecto.getEstado() != EstadoProyecto.DISPONIBLE) {
            throw new ReglaNegocioException("El proyecto no está disponible para postulaciones");
        }
        if (postulacionRepository.existsByAlumnoNumeroControlAndProyectoIdProyecto(alumno.getNumeroControl(), proyecto.getIdProyecto())) {
            throw new ReglaNegocioException("Ya te postulaste a este proyecto");
        }
        Postulacion postulacion = new Postulacion();
        postulacion.setAlumno(alumno);
        postulacion.setProyecto(proyecto);
        postulacion.setFechaSolicitud(LocalDate.now());
        postulacion.setEstado(EstadoPostulacion.EN_REVISION);
        return PostulacionResponse.de(postulacionRepository.save(postulacion));
    }

    @Transactional
    public PostulacionResponse cambiarEstado(Long id, CambioEstadoPostulacionRequest request, UsuarioActual actual) {
        Postulacion postulacion = buscar(id);
        proyectoService.verificarPropietario(postulacion.getProyecto(), actual);
        if (postulacion.getEstado() != EstadoPostulacion.EN_REVISION) {
            throw new ReglaNegocioException("Solo se pueden resolver postulaciones en revisión");
        }
        switch (request.estado()) {
            case ACEPTADO -> aceptar(postulacion);
            case RECHAZADO -> postulacion.setEstado(EstadoPostulacion.RECHAZADO);
            case EN_REVISION -> throw new ReglaNegocioException("El nuevo estado debe ser ACEPTADO o RECHAZADO");
        }
        postulacion.setComentariosEmpresa(request.comentarios());
        return PostulacionResponse.de(postulacion);
    }

    private void aceptar(Postulacion postulacion) {
        ProyectoResidencia proyecto = postulacion.getProyecto();
        Alumno alumno = postulacion.getAlumno();
        if (proyecto.getEstado() != EstadoProyecto.DISPONIBLE) {
            throw new ReglaNegocioException("El proyecto ya no está disponible");
        }
        long aceptados = postulacionRepository.countByProyectoIdProyectoAndEstado(proyecto.getIdProyecto(), EstadoPostulacion.ACEPTADO);
        if (aceptados >= proyecto.getCupoAlumnos()) {
            throw new ReglaNegocioException("El proyecto ya completó su cupo de " + proyecto.getCupoAlumnos() + " alumnos");
        }
        if (alumno.getEstadoResidencia() != EstadoResidencia.PENDIENTE) {
            throw new ReglaNegocioException("El alumno ya fue aceptado en otro proyecto");
        }

        postulacion.setEstado(EstadoPostulacion.ACEPTADO);
        alumno.setEstadoResidencia(EstadoResidencia.EN_PROCESO);

        // El alumno solo puede tener una residencia: se rechazan sus otras solicitudes pendientes
        rechazarPendientes(postulacionRepository.findByAlumnoNumeroControlAndEstado(alumno.getNumeroControl(), EstadoPostulacion.EN_REVISION),
                postulacion, "Rechazada automáticamente: el alumno fue aceptado en otro proyecto");

        if (aceptados + 1 >= proyecto.getCupoAlumnos()) {
            proyecto.setEstado(EstadoProyecto.ASIGNADO);
            rechazarPendientes(postulacionRepository.findByProyectoIdProyecto(proyecto.getIdProyecto()),
                    postulacion, "Rechazada automáticamente: el proyecto completó su cupo");
        }
    }

    private void rechazarPendientes(List<Postulacion> postulaciones, Postulacion aceptada, String motivo) {
        postulaciones.stream()
                .filter(p -> p.getEstado() == EstadoPostulacion.EN_REVISION && !p.getIdPostulacion().equals(aceptada.getIdPostulacion()))
                .forEach(p -> {
                    p.setEstado(EstadoPostulacion.RECHAZADO);
                    p.setComentariosEmpresa(motivo);
                });
    }

    @Transactional
    public PostulacionResponse asignarAsesor(Long id, Long idAsesor) {
        Postulacion postulacion = buscar(id);
        if (postulacion.getEstado() != EstadoPostulacion.ACEPTADO
                || postulacion.getAlumno().getEstadoResidencia() != EstadoResidencia.EN_PROCESO) {
            throw new ReglaNegocioException("Solo se puede asignar asesor a una residencia en curso");
        }
        AsesorAcademico asesor = asesorRepository.findById(idAsesor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asesor", idAsesor));
        if (postulacion.getAsesor() != null && postulacion.getAsesor().getIdAsesor().equals(idAsesor)) {
            return PostulacionResponse.de(postulacion);
        }
        long enSeguimiento = postulacionRepository.countByAsesorIdAsesorAndAlumnoEstadoResidencia(idAsesor, EstadoResidencia.EN_PROCESO);
        if (asesor.getMaximoAlumnos() != null && enSeguimiento >= asesor.getMaximoAlumnos()) {
            throw new ReglaNegocioException("El asesor ya alcanzó su máximo de " + asesor.getMaximoAlumnos() + " alumnos");
        }
        postulacion.setAsesor(asesor);
        return PostulacionResponse.de(postulacion);
    }

    // El asesor asignado (o un admin) da por terminada la residencia cuando el reporte final está aprobado
    @Transactional
    public PostulacionResponse finalizarResidencia(Long id, UsuarioActual actual) {
        Postulacion postulacion = buscar(id);
        if (!actual.esAdmin() && !esAsesorAsignado(postulacion, actual)) {
            throw new AccesoDenegadoException("Solo el asesor asignado puede finalizar la residencia");
        }
        if (postulacion.getEstado() != EstadoPostulacion.ACEPTADO
                || postulacion.getAlumno().getEstadoResidencia() != EstadoResidencia.EN_PROCESO) {
            throw new ReglaNegocioException("La residencia no está en curso");
        }
        if (!documentoRepository.existsByPostulacionIdPostulacionAndTipoDocumentoAndEstadoAprobacionTrue(id, TipoDocumento.REPORTE_FINAL)) {
            throw new ReglaNegocioException("Para finalizar la residencia el reporte final debe estar aprobado");
        }
        postulacion.getAlumno().setEstadoResidencia(EstadoResidencia.FINALIZADA);
        return PostulacionResponse.de(postulacion);
    }

    // El alumno puede cancelar su postulación mientras siga en revisión; el admin puede borrar cualquiera sin documentos
    @Transactional
    public void eliminar(Long id, UsuarioActual actual) {
        Postulacion postulacion = buscar(id);
        if (!actual.esAdmin()) {
            if (!esAlumnoPropietario(postulacion, actual)) {
                throw new AccesoDenegadoException("La postulación no te pertenece");
            }
            if (postulacion.getEstado() != EstadoPostulacion.EN_REVISION) {
                throw new ReglaNegocioException("Solo puedes cancelar postulaciones que siguen en revisión");
            }
        }
        if (documentoRepository.existsByPostulacionIdPostulacion(id)) {
            throw new ReglaNegocioException("La postulación tiene documentos en su expediente");
        }
        postulacionRepository.delete(postulacion);
    }

    public Postulacion buscar(Long id) {
        return postulacionRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Postulación", id));
    }

    // Pueden verla el admin, el alumno, la empresa del proyecto y el asesor asignado
    public void verificarAccesoLectura(Postulacion postulacion, UsuarioActual actual) {
        boolean permitido = actual.esAdmin()
                || esAlumnoPropietario(postulacion, actual)
                || postulacion.getProyecto().getEmpresa().getUsuario().getIdUsuario().equals(actual.idUsuario())
                || esAsesorAsignado(postulacion, actual);
        if (!permitido) {
            throw new AccesoDenegadoException("No tienes acceso a esta postulación");
        }
    }

    public boolean esAlumnoPropietario(Postulacion postulacion, UsuarioActual actual) {
        return postulacion.getAlumno().getUsuario().getIdUsuario().equals(actual.idUsuario());
    }

    public boolean esAsesorAsignado(Postulacion postulacion, UsuarioActual actual) {
        return postulacion.getAsesor() != null
                && postulacion.getAsesor().getUsuario().getIdUsuario().equals(actual.idUsuario());
    }

    private List<PostulacionResponse> aResponses(List<Postulacion> postulaciones) {
        return postulaciones.stream().map(PostulacionResponse::de).toList();
    }
}
