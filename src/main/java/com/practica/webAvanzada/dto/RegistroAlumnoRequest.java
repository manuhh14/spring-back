package com.practica.webAvanzada.dto;

import jakarta.validation.constraints.*;

public record RegistroAlumnoRequest(
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8, message = "debe tener al menos 8 caracteres") String password,
        @NotBlank @Size(max = 20) String numeroControl,
        @NotBlank String nombreCompleto,
        @NotBlank String carrera,
        @NotNull @Min(1) @Max(14) Integer semestre,
        String habilidadesTecnicas
) {
}
