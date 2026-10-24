package com.practica.webAvanzada.dto;

import com.practica.webAvanzada.models.Usuario;
import com.practica.webAvanzada.models.enums.RoleUsuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long idUsuario,
        String correo,
        RoleUsuario rol,
        LocalDateTime fechaRegistro,
        Boolean activo
) {
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getIdUsuario(), usuario.getCorreo(), usuario.getRol(),
                usuario.getFechaRegistro(), usuario.getActivo());
    }
}
