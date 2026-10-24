package com.practica.webAvanzada.dto;

import com.practica.webAvanzada.models.ExpedienteDocumento;
import com.practica.webAvanzada.models.enums.TipoDocumento;

import java.time.LocalDate;

// estadoAprobacion: null = pendiente de revisión, true = aprobado, false = rechazado
public record DocumentoResponse(
        Long idDocumento,
        Long idPostulacion,
        TipoDocumento tipoDocumento,
        String nombreArchivo,
        String urlDescarga,
        LocalDate fechaSubida,
        Boolean estadoAprobacion
) {
    public static DocumentoResponse de(ExpedienteDocumento documento) {
        return new DocumentoResponse(documento.getIdDocumento(), documento.getPostulacion().getIdPostulacion(),
                documento.getTipoDocumento(), documento.getNombreArchivoOriginal(),
                "/api/documentos/" + documento.getIdDocumento() + "/archivo",
                documento.getFechaSubida(), documento.getEstadoAprobacion());
    }
}
