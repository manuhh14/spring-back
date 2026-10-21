package com.practica.webAvanzada.services;

import com.practica.webAvanzada.models.Usuario;
import com.practica.webAvanzada.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // Inyectamos el encriptador

    public Usuario guardar(Usuario usuario) {
        // Encriptamos el texto plano antes de mandarlo a la base de datos
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario actualizar(Long id, Usuario usuarioActualizado) {
        if (usuarioRepository.existsById(id)) {
            usuarioActualizado.setIdUsuario(id);
            // Si también quieres permitir actualizar la contraseña, deberías encriptarla aquí de nuevo
            return usuarioRepository.save(usuarioActualizado);
        }
        return null;
    }

    // NUEVO MÉTODO: Lógica de Login
    public Usuario login(String correo, String passwordPlano) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Comparamos el password plano que llega de Postman con el Hash de la BD
            if (passwordEncoder.matches(passwordPlano, usuario.getPassword())) {
                return usuario; // Login exitoso
            }
        }
        return null;
    }
}