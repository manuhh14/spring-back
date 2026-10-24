package com.practica.webAvanzada.services;

import com.practica.webAvanzada.dto.CambioEstadoPostulacionRequest;
import com.practica.webAvanzada.dto.PostulacionRequest;
import com.practica.webAvanzada.exceptions.AccesoDenegadoException;
import com.practica.webAvanzada.exceptions.ReglaNegocioException;
import com.practica.webAvanzada.models.*;
import com.practica.webAvanzada.models.enums.*;
import com.practica.webAvanzada.repositories.AsesorAcademicoRepository;
import com.practica.webAvanzada.repositories.ExpedienteDocumentoRepository;
import com.practica.webAvanzada.repositories.PostulacionRepository;
import com.practica.webAvanzada.security.UsuarioActual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostulacionServiceTest {

    @Mock private PostulacionRepository postulacionRepository;
    @Mock private AsesorAcademicoRepository asesorRepository;
    @Mock private ExpedienteDocumentoRepository documentoRepository;
    @Mock private AlumnoService alumnoService;
    @Mock private AsesorAcademicoService asesorService;
    @Mock private ProyectoResidenciaService proyectoService;

    @InjectMocks
    private PostulacionService postulacionService;

    private final UsuarioActual usuarioAlumno = new UsuarioActual(1L, RoleUsuario.ALUMNO);
    private final UsuarioActual usuarioEmpresa = new UsuarioActual(2L, RoleUsuario.EMPRESA);
    private Alumno alumno;
    private ProyectoResidencia proyecto;

    @BeforeEach
    void preparar() {
        alumno = new Alumno();
        alumno.setNumeroControl("20240001");
        alumno.setUsuario(usuario(1L));
        alumno.setEstadoResidencia(EstadoResidencia.PENDIENTE);

        Empresa empresa = new Empresa();
        empresa.setIdEmpresa(10L);
        empresa.setUsuario(usuario(2L));
        empresa.setRazonSocial("ACME");

        proyecto = new ProyectoResidencia();
        proyecto.setIdProyecto(100L);
        proyecto.setEmpresa(empresa);
        proyecto.setCupoAlumnos(2);
        proyecto.setEstado(EstadoProyecto.DISPONIBLE);
    }

    @Test
    void crearRechazaPostulacionDuplicada() {
        when(alumnoService.buscarPorUsuario(1L)).thenReturn(alumno);
        when(proyectoService.buscar(100L)).thenReturn(proyecto);
        when(postulacionRepository.existsByAlumnoNumeroControlAndProyectoIdProyecto("20240001", 100L)).thenReturn(true);

        assertThatThrownBy(() -> postulacionService.crear(new PostulacionRequest(100L), usuarioAlumno))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya te postulaste");
        verify(postulacionRepository, never()).save(any());
    }

    @Test
    void crearRechazaProyectoNoDisponible() {
        proyecto.setEstado(EstadoProyecto.CERRADO);
        when(alumnoService.buscarPorUsuario(1L)).thenReturn(alumno);
        when(proyectoService.buscar(100L)).thenReturn(proyecto);

        assertThatThrownBy(() -> postulacionService.crear(new PostulacionRequest(100L), usuarioAlumno))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void crearRechazaAlumnoConResidenciaEnCurso() {
        alumno.setEstadoResidencia(EstadoResidencia.EN_PROCESO);
        when(alumnoService.buscarPorUsuario(1L)).thenReturn(alumno);

        assertThatThrownBy(() -> postulacionService.crear(new PostulacionRequest(100L), usuarioAlumno))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void aceptarCompletaCupoYRechazaPendientes() {
        proyecto.setCupoAlumnos(1);
        Postulacion postulacion = postulacion(1L, alumno, EstadoPostulacion.EN_REVISION);
        Postulacion otraDelAlumno = postulacion(2L, alumno, EstadoPostulacion.EN_REVISION);
        Alumno otroAlumno = new Alumno();
        otroAlumno.setNumeroControl("20240002");
        Postulacion otraDelProyecto = postulacion(3L, otroAlumno, EstadoPostulacion.EN_REVISION);

        when(postulacionRepository.findById(1L)).thenReturn(Optional.of(postulacion));
        when(postulacionRepository.countByProyectoIdProyectoAndEstado(100L, EstadoPostulacion.ACEPTADO)).thenReturn(0L);
        when(postulacionRepository.findByAlumnoNumeroControlAndEstado("20240001", EstadoPostulacion.EN_REVISION))
                .thenReturn(List.of(postulacion, otraDelAlumno));
        when(postulacionRepository.findByProyectoIdProyecto(100L)).thenReturn(List.of(postulacion, otraDelProyecto));

        var respuesta = postulacionService.cambiarEstado(1L,
                new CambioEstadoPostulacionRequest(EstadoPostulacion.ACEPTADO, "Bienvenido"), usuarioEmpresa);

        assertThat(respuesta.estado()).isEqualTo(EstadoPostulacion.ACEPTADO);
        assertThat(alumno.getEstadoResidencia()).isEqualTo(EstadoResidencia.EN_PROCESO);
        assertThat(proyecto.getEstado()).isEqualTo(EstadoProyecto.ASIGNADO);
        assertThat(otraDelAlumno.getEstado()).isEqualTo(EstadoPostulacion.RECHAZADO);
        assertThat(otraDelProyecto.getEstado()).isEqualTo(EstadoPostulacion.RECHAZADO);
    }

    @Test
    void aceptarFallaSiElCupoEstaLleno() {
        Postulacion postulacion = postulacion(1L, alumno, EstadoPostulacion.EN_REVISION);
        when(postulacionRepository.findById(1L)).thenReturn(Optional.of(postulacion));
        when(postulacionRepository.countByProyectoIdProyectoAndEstado(100L, EstadoPostulacion.ACEPTADO)).thenReturn(2L);

        assertThatThrownBy(() -> postulacionService.cambiarEstado(1L,
                new CambioEstadoPostulacionRequest(EstadoPostulacion.ACEPTADO, null), usuarioEmpresa))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cupo");
        assertThat(alumno.getEstadoResidencia()).isEqualTo(EstadoResidencia.PENDIENTE);
    }

    @Test
    void noSePuedeResolverDosVeces() {
        Postulacion postulacion = postulacion(1L, alumno, EstadoPostulacion.RECHAZADO);
        when(postulacionRepository.findById(1L)).thenReturn(Optional.of(postulacion));

        assertThatThrownBy(() -> postulacionService.cambiarEstado(1L,
                new CambioEstadoPostulacionRequest(EstadoPostulacion.ACEPTADO, null), usuarioEmpresa))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void asignarAsesorRespetaSuMaximo() {
        alumno.setEstadoResidencia(EstadoResidencia.EN_PROCESO);
        Postulacion postulacion = postulacion(1L, alumno, EstadoPostulacion.ACEPTADO);
        AsesorAcademico asesor = new AsesorAcademico();
        asesor.setIdAsesor(7L);
        asesor.setMaximoAlumnos(3);
        when(postulacionRepository.findById(1L)).thenReturn(Optional.of(postulacion));
        when(asesorRepository.findById(7L)).thenReturn(Optional.of(asesor));
        when(postulacionRepository.countByAsesorIdAsesorAndAlumnoEstadoResidencia(7L, EstadoResidencia.EN_PROCESO)).thenReturn(3L);

        assertThatThrownBy(() -> postulacionService.asignarAsesor(1L, 7L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("máximo");
        assertThat(postulacion.getAsesor()).isNull();
    }

    @Test
    void alumnoNoPuedeCancelarPostulacionAjena() {
        Alumno otro = new Alumno();
        otro.setUsuario(usuario(99L));
        Postulacion postulacion = postulacion(1L, otro, EstadoPostulacion.EN_REVISION);
        when(postulacionRepository.findById(1L)).thenReturn(Optional.of(postulacion));

        assertThatThrownBy(() -> postulacionService.eliminar(1L, usuarioAlumno))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(postulacionRepository, never()).delete(any());
    }

    private Postulacion postulacion(Long id, Alumno alumno, EstadoPostulacion estado) {
        Postulacion postulacion = new Postulacion();
        postulacion.setIdPostulacion(id);
        postulacion.setAlumno(alumno);
        postulacion.setProyecto(proyecto);
        postulacion.setEstado(estado);
        return postulacion;
    }

    private static Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        return usuario;
    }
}
