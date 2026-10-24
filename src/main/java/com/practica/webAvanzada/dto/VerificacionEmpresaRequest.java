package com.practica.webAvanzada.dto;

import jakarta.validation.constraints.NotNull;

public record VerificacionEmpresaRequest(
        @NotNull Boolean verificada
) {
}
