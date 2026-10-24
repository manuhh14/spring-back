package com.practica.webAvanzada.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroEmpresaRequest(
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8, message = "debe tener al menos 8 caracteres") String password,
        @NotBlank String razonSocial,
        @Size(max = 100) String sector,
        @NotBlank String nombreContacto,
        @Size(max = 20) String telefono,
        String direccion
) {
}
