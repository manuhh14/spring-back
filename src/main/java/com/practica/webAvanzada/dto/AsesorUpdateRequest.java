package com.practica.webAvanzada.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AsesorUpdateRequest(
        @NotBlank String nombreCompleto,
        String departamento,
        @NotNull @Min(1) Integer maximoAlumnos
) {
}
