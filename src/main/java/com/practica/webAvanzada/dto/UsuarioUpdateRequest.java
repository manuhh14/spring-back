package com.practica.webAvanzada.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// password es opcional: si no se envía se conserva la actual
public record UsuarioUpdateRequest(
        @NotBlank @Email String correo,
        @Size(min = 8, message = "debe tener al menos 8 caracteres") String password,
        @NotNull Boolean activo
) {
}
