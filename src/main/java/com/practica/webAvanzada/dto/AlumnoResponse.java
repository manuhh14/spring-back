package com.practica.webAvanzada.dto;

import com.practica.webAvanzada.models.Alumno;
import com.practica.webAvanzada.models.enums.EstadoResidencia;

public record AlumnoResponse(
        String numeroControl,
        Long idUsuario,
        String correo,
        String nombreCompleto,
        String carrera,
        int semestre,
        String habilidadesTecnicas,
        EstadoResidencia estadoResidencia
) {
    public static AlumnoResponse de(Alumno alumno) {
        return new AlumnoResponse(alumno.getNumeroControl(), alumno.getUsuario().getIdUsuario(),
                alumno.getUsuario().getCorreo(), alumno.getNombreCompleto(), alumno.getCarrera(),
                alumno.getSemestre(), alumno.getHabilidadesTecnicas(), alumno.getEstadoResidencia());
    }
}
