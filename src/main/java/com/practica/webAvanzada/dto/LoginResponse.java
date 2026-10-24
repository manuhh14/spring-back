package com.practica.webAvanzada.dto;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        UsuarioResponse usuario
) {
}
