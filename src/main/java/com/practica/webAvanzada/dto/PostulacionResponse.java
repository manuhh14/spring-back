package com.practica.webAvanzada.dto;

import com.practica.webAvanzada.models.AsesorAcademico;
import com.practica.webAvanzada.models.Postulacion;
import com.practica.webAvanzada.models.enums.EstadoPostulacion;
import com.practica.webAvanzada.models.enums.EstadoResidencia;

import java.time.LocalDate;

public record PostulacionResponse(
        Long idPostulacion,
        String numeroControl,
        String nombreAlumno,
        EstadoResidencia estadoResidencia,
        Long idProyecto,
        String tituloProyecto,
        String razonSocialEmpresa,
        LocalDate fechaSolicitud,
        EstadoPostulacion estado,
        String comentariosEmpresa,
        Long idAsesor,
        String nombreAsesor
) {
    public static PostulacionResponse de(Postulacion postulacion) {
        AsesorAcademico asesor = postulacion.getAsesor();
        return new PostulacionResponse(postulacion.getIdPostulacion(),
                postulacion.getAlumno().getNumeroControl(), postulacion.getAlumno().getNombreCompleto(),
                postulacion.getAlumno().getEstadoResidencia(), postulacion.getProyecto().getIdProyecto(),
                postulacion.getProyecto().getTitulo(), postulacion.getProyecto().getEmpresa().getRazonSocial(),
                postulacion.getFechaSolicitud(), postulacion.getEstado(), postulacion.getComentariosEmpresa(),
                asesor != null ? asesor.getIdAsesor() : null,
                asesor != null ? asesor.getNombreCompleto() : null);
    }
}
