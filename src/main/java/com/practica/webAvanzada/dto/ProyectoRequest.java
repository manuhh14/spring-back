package com.practica.webAvanzada.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProyectoRequest(
        @NotBlank @Size(max = 255) String titulo,
        String descripcion,
        String objetivo,
        String tecnologiasRequeridas,
        @NotNull @Min(1) Integer cupoAlumnos
) {
}
