package com.practica.webAvanzada.services;

import com.practica.webAvanzada.dto.AsesorRequest;
import com.practica.webAvanzada.dto.AsesorResponse;
import com.practica.webAvanzada.dto.AsesorUpdateRequest;
import com.practica.webAvanzada.exceptions.RecursoNoEncontradoException;
import com.practica.webAvanzada.exceptions.ReglaNegocioException;
import com.practica.webAvanzada.models.AsesorAcademico;
import com.practica.webAvanzada.models.enums.EstadoResidencia;
import com.practica.webAvanzada.models.enums.RoleUsuario;
import com.practica.webAvanzada.repositories.AsesorAcademicoRepository;
import com.practica.webAvanzada.repositories.PostulacionRepository;
import com.practica.webAvanzada.repositories.UsuarioRepository;
import com.practica.webAvanzada.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AsesorAcademicoService {
    private final AsesorAcademicoRepository asesorRepository;
    private final PostulacionRepository postulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public List<AsesorResponse> obtenerTodos() {
        return asesorRepository.findAll().stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public AsesorResponse obtenerPorId(Long id) {
        return aResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public AsesorResponse obtenerPerfil(UsuarioActual actual) {
        return aResponse(buscarPorUsuario(actual.idUsuario()));
    }

    @Transactional
    public AsesorResponse crear(AsesorRequest request) {
        AsesorAcademico asesor = new AsesorAcademico();
        asesor.setUsuario(usuarioService.crearUsuario(request.correo(), request.password(), RoleUsuario.ASESOR));
        asesor.setNombreCompleto(request.nombreCompleto());
        asesor.setDepartamento(request.departamento());
        asesor.setMaximoAlumnos(request.maximoAlumnos());
        return aResponse(asesorRepository.save(asesor));
    }

    @Transactional
    public AsesorResponse actualizar(Long id, AsesorUpdateRequest request) {
        AsesorAcademico asesor = buscar(id);
        long enSeguimiento = alumnosEnSeguimiento(asesor);
        if (request.maximoAlumnos() < enSeguimiento) {
            throw new ReglaNegocioException("El asesor ya tiene " + enSeguimiento
                    + " alumnos en seguimiento; el máximo no puede ser menor");
        }
        asesor.setNombreCompleto(request.nombreCompleto());
        asesor.setDepartamento(request.departamento());
        asesor.setMaximoAlumnos(request.maximoAlumnos());
        return aResponse(asesor);
    }

    @Transactional
    public void eliminar(Long id) {
        AsesorAcademico asesor = buscar(id);
        if (postulacionRepository.existsByAsesorIdAsesor(id)) {
            throw new ReglaNegocioException("El asesor tiene residencias asignadas; desactiva su usuario en su lugar");
        }
        asesorRepository.delete(asesor);
        usuarioRepository.delete(asesor.getUsuario());
    }

    public AsesorAcademico buscarPorUsuario(Long idUsuario) {
        return asesorRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de asesor del usuario", idUsuario));
    }

    private AsesorAcademico buscar(Long id) {
        return asesorRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Asesor", id));
    }

    private long alumnosEnSeguimiento(AsesorAcademico asesor) {
        return postulacionRepository.countByAsesorIdAsesorAndAlumnoEstadoResidencia(asesor.getIdAsesor(), EstadoResidencia.EN_PROCESO);
    }

    private AsesorResponse aResponse(AsesorAcademico asesor) {
        return AsesorResponse.de(asesor, alumnosEnSeguimiento(asesor));
    }
}
