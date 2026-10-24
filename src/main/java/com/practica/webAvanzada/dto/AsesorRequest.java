package com.practica.webAvanzada.dto;

import jakarta.validation.constraints.*;

public record AsesorRequest(
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8, message = "debe tener al menos 8 caracteres") String password,
        @NotBlank String nombreCompleto,
        String departamento,
        @NotNull @Min(1) Integer maximoAlumnos
) {
}
