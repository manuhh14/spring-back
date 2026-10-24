package com.practica.webAvanzada.dto;

import com.practica.webAvanzada.models.enums.EstadoPostulacion;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoPostulacionRequest(
        @NotNull EstadoPostulacion estado,
        String comentarios
) {
}
