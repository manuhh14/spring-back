package com.practica.webAvanzada.dto;

import jakarta.validation.constraints.NotNull;

public record AsignarAsesorRequest(
        @NotNull Long idAsesor
) {
}
