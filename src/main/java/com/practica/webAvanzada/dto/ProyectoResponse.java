package com.practica.webAvanzada.dto;

import com.practica.webAvanzada.models.ProyectoResidencia;
import com.practica.webAvanzada.models.enums.EstadoProyecto;

import java.time.LocalDate;

public record ProyectoResponse(
        Long idProyecto,
        Long idEmpresa,
        String razonSocialEmpresa,
        String titulo,
        String descripcion,
        String objetivo,
        String tecnologiasRequeridas,
        Integer cupoAlumnos,
        long alumnosAceptados,
        EstadoProyecto estado,
        LocalDate fechaPublicacion
) {
    public static ProyectoResponse de(ProyectoResidencia proyecto, long alumnosAceptados) {
        return new ProyectoResponse(proyecto.getIdProyecto(), proyecto.getEmpresa().getIdEmpresa(),
                proyecto.getEmpresa().getRazonSocial(), proyecto.getTitulo(), proyecto.getDescripcion(),
                proyecto.getObjetivo(), proyecto.getTecnologiasRequeridas(), proyecto.getCupoAlumnos(),
                alumnosAceptados, proyecto.getEstado(), proyecto.getFechaPublicacion());
    }
}
