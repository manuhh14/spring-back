package com.practica.webAvanzada.dto;

import jakarta.validation.constraints.NotNull;

public record PostulacionRequest(
        @NotNull Long idProyecto
) {
}
