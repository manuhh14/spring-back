package com.practica.webAvanzada.dto;

import com.practica.webAvanzada.models.AsesorAcademico;

public record AsesorResponse(
        Long idAsesor,
        Long idUsuario,
        String correo,
        String nombreCompleto,
        String departamento,
        Integer maximoAlumnos,
        long alumnosEnSeguimiento
) {
    public static AsesorResponse de(AsesorAcademico asesor, long alumnosEnSeguimiento) {
        return new AsesorResponse(asesor.getIdAsesor(), asesor.getUsuario().getIdUsuario(),
                asesor.getUsuario().getCorreo(), asesor.getNombreCompleto(), asesor.getDepartamento(),
                asesor.getMaximoAlumnos(), alumnosEnSeguimiento);
    }
}
