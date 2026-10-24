package com.practica.webAvanzada.services;

import com.practica.webAvanzada.dto.DocumentoResponse;
import com.practica.webAvanzada.exceptions.AccesoDenegadoException;
import com.practica.webAvanzada.exceptions.RecursoNoEncontradoException;
import com.practica.webAvanzada.exceptions.ReglaNegocioException;
import com.practica.webAvanzada.models.ExpedienteDocumento;
import com.practica.webAvanzada.models.Postulacion;
import com.practica.webAvanzada.models.enums.EstadoPostulacion;
import com.practica.webAvanzada.models.enums.EstadoResidencia;
import com.practica.webAvanzada.models.enums.TipoDocumento;
import com.practica.webAvanzada.repositories.ExpedienteDocumentoRepository;
import com.practica.webAvanzada.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpedienteDocumentoService {
    private final ExpedienteDocumentoRepository documentoRepository;
    private final PostulacionService postulacionService;
    private final AlmacenamientoArchivosService almacenamiento;

    public record ArchivoDescarga(Resource recurso, String nombre) {
    }

    @Transactional(readOnly = true)
    public List<DocumentoResponse> obtenerTodos() {
        return documentoRepository.findAll().stream().map(DocumentoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public DocumentoResponse obtenerPorId(Long id, UsuarioActual actual) {
        ExpedienteDocumento documento = buscar(id);
        postulacionService.verificarAccesoLectura(documento.getPostulacion(), actual);
        return DocumentoResponse.de(documento);
    }

    @Transactional(readOnly = true)
    public List<DocumentoResponse> obtenerPorPostulacion(Long idPostulacion, UsuarioActual actual) {
        postulacionService.verificarAccesoLectura(postulacionService.buscar(idPostulacion), actual);
        return documentoRepository.findByPostulacionIdPostulacion(idPostulacion).stream().map(DocumentoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ArchivoDescarga descargar(Long id, UsuarioActual actual) {
        ExpedienteDocumento documento = buscar(id);
        postulacionService.verificarAccesoLectura(documento.getPostulacion(), actual);
        String nombre = documento.getNombreArchivoOriginal() != null ? documento.getNombreArchivoOriginal() : documento.getRutaArchivoUrl();
        return new ArchivoDescarga(almacenamiento.cargar(documento.getRutaArchivoUrl()), nombre);
    }

    @Transactional
    public DocumentoResponse subir(Long idPostulacion, TipoDocumento tipo, MultipartFile archivo, UsuarioActual actual) {
        Postulacion postulacion = postulacionService.buscar(idPostulacion);
        if (!postulacionService.esAlumnoPropietario(postulacion, actual)) {
            throw new AccesoDenegadoException("La postulación no te pertenece");
        }
        if (postulacion.getEstado() != EstadoPostulacion.ACEPTADO
                || postulacion.getAlumno().getEstadoResidencia() != EstadoResidencia.EN_PROCESO) {
            throw new ReglaNegocioException("Solo se pueden subir documentos de una residencia en curso");
        }
        // Puede haber varios reportes bimestrales, pero solo un anteproyecto y un reporte final aprobados
        if (tipo != TipoDocumento.REPORTE_BIMESTRAL
                && documentoRepository.existsByPostulacionIdPostulacionAndTipoDocumentoAndEstadoAprobacionTrue(idPostulacion, tipo)) {
            throw new ReglaNegocioException("Ya existe un " + tipo + " aprobado en el expediente");
        }
        ExpedienteDocumento documento = new ExpedienteDocumento();
        documento.setPostulacion(postulacion);
        documento.setTipoDocumento(tipo);
        documento.setRutaArchivoUrl(almacenamiento.guardarPdf(archivo));
        documento.setNombreArchivoOriginal(archivo.getOriginalFilename());
        documento.setFechaSubida(LocalDate.now());
        return DocumentoResponse.de(documentoRepository.save(documento));
    }

    @Transactional
    public DocumentoResponse revisar(Long id, boolean aprobado, UsuarioActual actual) {
        ExpedienteDocumento documento = buscar(id);
        if (!actual.esAdmin() && !postulacionService.esAsesorAsignado(documento.getPostulacion(), actual)) {
            throw new AccesoDenegadoException("Solo el asesor asignado puede revisar este documento");
        }
        documento.setEstadoAprobacion(aprobado);
        return DocumentoResponse.de(documento);
    }

    // El alumno puede retirar un documento que aún no ha sido aprobado
    @Transactional
    public void eliminar(Long id, UsuarioActual actual) {
        ExpedienteDocumento documento = buscar(id);
        if (!actual.esAdmin()) {
            if (!postulacionService.esAlumnoPropietario(documento.getPostulacion(), actual)) {
                throw new AccesoDenegadoException("El documento no te pertenece");
            }
            if (Boolean.TRUE.equals(documento.getEstadoAprobacion())) {
                throw new ReglaNegocioException("No se puede eliminar un documento aprobado");
            }
        }
        documentoRepository.delete(documento);
        almacenamiento.eliminar(documento.getRutaArchivoUrl());
    }

    private ExpedienteDocumento buscar(Long id) {
        return documentoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Documento", id));
    }
}
