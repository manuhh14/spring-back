package com.practica.webAvanzada.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlumnoUpdateRequest(
        @NotBlank String nombreCompleto,
        @NotBlank String carrera,
        @NotNull @Min(1) @Max(14) Integer semestre,
        String habilidadesTecnicas
) {
}
