package com.practica.webAvanzada.services;

import com.practica.webAvanzada.dto.AlumnoResponse;
import com.practica.webAvanzada.dto.AlumnoUpdateRequest;
import com.practica.webAvanzada.dto.RegistroAlumnoRequest;
import com.practica.webAvanzada.exceptions.AccesoDenegadoException;
import com.practica.webAvanzada.exceptions.RecursoNoEncontradoException;
import com.practica.webAvanzada.exceptions.ReglaNegocioException;
import com.practica.webAvanzada.models.Alumno;
import com.practica.webAvanzada.models.enums.EstadoResidencia;
import com.practica.webAvanzada.models.enums.RoleUsuario;
import com.practica.webAvanzada.repositories.AlumnoRepository;
import com.practica.webAvanzada.repositories.PostulacionRepository;
import com.practica.webAvanzada.repositories.UsuarioRepository;
import com.practica.webAvanzada.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlumnoService {
    private final AlumnoRepository alumnoRepository;
    private final PostulacionRepository postulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public Page<AlumnoResponse> obtenerTodos(Pageable pageable) {
        return alumnoRepository.findAll(pageable).map(AlumnoResponse::de);
    }

    // Un alumno solo puede ver su propio perfil; empresas, asesores y admin pueden ver cualquiera
    @Transactional(readOnly = true)
    public AlumnoResponse obtenerPorId(String numeroControl, UsuarioActual actual) {
        Alumno alumno = buscar(numeroControl);
        if (actual.rol() == RoleUsuario.ALUMNO && !esPropietario(alumno, actual)) {
            throw new AccesoDenegadoException("Solo puedes consultar tu propio perfil");
        }
        return AlumnoResponse.de(alumno);
    }

    @Transactional(readOnly = true)
    public AlumnoResponse obtenerPerfil(UsuarioActual actual) {
        return AlumnoResponse.de(buscarPorUsuario(actual.idUsuario()));
    }

    @Transactional
    public AlumnoResponse registrar(RegistroAlumnoRequest request) {
        if (alumnoRepository.existsById(request.numeroControl())) {
            throw new ReglaNegocioException("Ya existe un alumno con el número de control " + request.numeroControl());
        }
        Alumno alumno = new Alumno();
        alumno.setNumeroControl(request.numeroControl());
        alumno.setUsuario(usuarioService.crearUsuario(request.correo(), request.password(), RoleUsuario.ALUMNO));
        alumno.setNombreCompleto(request.nombreCompleto());
        alumno.setCarrera(request.carrera());
        alumno.setSemestre(request.semestre());
        alumno.setHabilidadesTecnicas(request.habilidadesTecnicas());
        alumno.setEstadoResidencia(EstadoResidencia.PENDIENTE);
        return AlumnoResponse.de(alumnoRepository.save(alumno));
    }

    @Transactional
    public AlumnoResponse actualizar(String numeroControl, AlumnoUpdateRequest request, UsuarioActual actual) {
        Alumno alumno = buscar(numeroControl);
        if (!actual.esAdmin() && !esPropietario(alumno, actual)) {
            throw new AccesoDenegadoException("Solo puedes modificar tu propio perfil");
        }
        alumno.setNombreCompleto(request.nombreCompleto());
        alumno.setCarrera(request.carrera());
        alumno.setSemestre(request.semestre());
        alumno.setHabilidadesTecnicas(request.habilidadesTecnicas());
        return AlumnoResponse.de(alumno);
    }

    @Transactional
    public void eliminar(String numeroControl) {
        Alumno alumno = buscar(numeroControl);
        if (postulacionRepository.existsByAlumnoNumeroControl(numeroControl)) {
            throw new ReglaNegocioException("El alumno tiene postulaciones registradas; desactiva su usuario en su lugar");
        }
        alumnoRepository.delete(alumno);
        usuarioRepository.delete(alumno.getUsuario());
    }

    public Alumno buscarPorUsuario(Long idUsuario) {
        return alumnoRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de alumno del usuario", idUsuario));
    }

    private Alumno buscar(String numeroControl) {
        return alumnoRepository.findById(numeroControl)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno", numeroControl));
    }

    private boolean esPropietario(Alumno alumno, UsuarioActual actual) {
        return alumno.getUsuario().getIdUsuario().equals(actual.idUsuario());
    }
}
