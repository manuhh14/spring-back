package com.practica.webAvanzada.dto;

import jakarta.validation.constraints.NotNull;

public record RevisionDocumentoRequest(
        @NotNull Boolean aprobado
) {
}
