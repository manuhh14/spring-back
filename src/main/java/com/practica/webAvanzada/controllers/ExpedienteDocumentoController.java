package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.dto.DocumentoResponse;
import com.practica.webAvanzada.dto.RevisionDocumentoRequest;
import com.practica.webAvanzada.models.enums.TipoDocumento;
import com.practica.webAvanzada.security.UsuarioActual;
import com.practica.webAvanzada.services.ExpedienteDocumentoService;
import com.practica.webAvanzada.services.ExpedienteDocumentoService.ArchivoDescarga;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "Expediente de documentos")
@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class ExpedienteDocumentoController {

    private final ExpedienteDocumentoService documentoService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<DocumentoResponse>> listarTodos() {
        return ResponseEntity.ok(documentoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoResponse> obtenerPorId(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(documentoService.obtenerPorId(id, UsuarioActual.de(jwt)));
    }

    @Operation(summary = "Descargar el PDF del documento")
    @GetMapping("/{id}/archivo")
    public ResponseEntity<Resource> descargar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        ArchivoDescarga archivo = documentoService.descargar(id, UsuarioActual.de(jwt));
        ContentDisposition disposicion = ContentDisposition.attachment()
                .filename(archivo.nombre(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .body(archivo.recurso());
    }

    @Operation(summary = "Subir un PDF al expediente de una residencia en curso")
    @PreAuthorize("hasRole('ALUMNO')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResponse> subir(@RequestParam Long idPostulacion,
                                                   @RequestParam TipoDocumento tipoDocumento,
                                                   @RequestPart("archivo") MultipartFile archivo,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentoService.subir(idPostulacion, tipoDocumento, archivo, UsuarioActual.de(jwt)));
    }

    @Operation(summary = "Aprobar o rechazar un documento (asesor asignado o admin)")
    @PreAuthorize("hasAnyRole('ASESOR', 'ADMIN')")
    @PatchMapping("/{id}/revision")
    public ResponseEntity<DocumentoResponse> revisar(@PathVariable Long id, @Valid @RequestBody RevisionDocumentoRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(documentoService.revisar(id, request.aprobado(), UsuarioActual.de(jwt)));
    }

    @PreAuthorize("hasAnyRole('ALUMNO', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        documentoService.eliminar(id, UsuarioActual.de(jwt));
        return ResponseEntity.noContent().build();
    }
}
