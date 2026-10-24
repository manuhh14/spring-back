package com.practica.webAvanzada.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmpresaUpdateRequest(
        @NotBlank String razonSocial,
        @Size(max = 100) String sector,
        @NotBlank String nombreContacto,
        @Size(max = 20) String telefono,
        String direccion
) {
}
