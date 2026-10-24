package com.practica.webAvanzada.services;

import com.practica.webAvanzada.dto.*;
import com.practica.webAvanzada.exceptions.CredencialesInvalidasException;
import com.practica.webAvanzada.exceptions.RecursoNoEncontradoException;
import com.practica.webAvanzada.exceptions.ReglaNegocioException;
import com.practica.webAvanzada.models.Usuario;
import com.practica.webAvanzada.models.enums.RoleUsuario;
import com.practica.webAvanzada.repositories.UsuarioRepository;
import com.practica.webAvanzada.security.JwtService;
import com.practica.webAvanzada.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> obtenerTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(UsuarioResponse::de);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return UsuarioResponse.de(buscar(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerActual(UsuarioActual actual) {
        return UsuarioResponse.de(buscar(actual.idUsuario()));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        return UsuarioResponse.de(crearUsuario(request.correo(), request.password(), request.rol()));
    }

    @Transactional
    public Usuario crearUsuario(String correo, String passwordPlano, RoleUsuario rol) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ReglaNegocioException("Ya existe un usuario con el correo " + correo);
        }
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(passwordPlano));
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = buscar(id);
        if (!usuario.getCorreo().equals(request.correo()) && usuarioRepository.existsByCorreo(request.correo())) {
            throw new ReglaNegocioException("Ya existe un usuario con el correo " + request.correo());
        }
        usuario.setCorreo(request.correo());
        usuario.setActivo(request.activo());
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        return UsuarioResponse.de(usuario);
    }

    // Si el usuario tiene un perfil (alumno, empresa, asesor) la FK lo impide y se responde 409
    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.delete(buscar(id));
        usuarioRepository.flush();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.correo())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .filter(Usuario::getActivo)
                .orElseThrow(CredencialesInvalidasException::new);
        return new LoginResponse(jwtService.generarToken(usuario), "Bearer",
                jwtService.getExpiracionSegundos(), UsuarioResponse.de(usuario));
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }
}
