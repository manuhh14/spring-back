package com.practica.webAvanzada.security;

import com.practica.webAvanzada.models.enums.RoleUsuario;
import org.springframework.security.oauth2.jwt.Jwt;

// Datos del usuario autenticado extraídos del token, para que los servicios no dependan de Spring Security
public record UsuarioActual(Long idUsuario, RoleUsuario rol) {

    public static UsuarioActual de(Jwt jwt) {
        Number id = jwt.getClaim(JwtService.CLAIM_ID_USUARIO);
        return new UsuarioActual(id.longValue(), RoleUsuario.valueOf(jwt.getClaimAsString(JwtService.CLAIM_ROL)));
    }

    public boolean esAdmin() {
        return rol == RoleUsuario.ADMIN;
    }
}
