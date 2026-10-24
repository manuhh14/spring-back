package com.practica.webAvanzada.dto;

import com.practica.webAvanzada.models.enums.RoleUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8, message = "debe tener al menos 8 caracteres") String password,
        @NotNull RoleUsuario rol
) {
}
