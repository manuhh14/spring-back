package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.dto.*;
import com.practica.webAvanzada.security.UsuarioActual;
import com.practica.webAvanzada.services.ExpedienteDocumentoService;
import com.practica.webAvanzada.services.PostulacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Postulaciones")
@RestController
@RequestMapping("/api/postulaciones")
@RequiredArgsConstructor
public class PostulacionController {

    private final PostulacionService postulacionService;
    private final ExpedienteDocumentoService documentoService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<PostulacionResponse>> listarTodas(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(postulacionService.obtenerTodos(pageable));
    }

    @PreAuthorize("hasRole('ALUMNO')")
    @GetMapping("/mis-postulaciones")
    public ResponseEntity<List<PostulacionResponse>> misPostulaciones(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postulacionService.obtenerDeAlumnoActual(UsuarioActual.de(jwt)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostulacionResponse> obtenerPorId(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postulacionService.obtenerPorId(id, UsuarioActual.de(jwt)));
    }

    @Operation(summary = "Documentos del expediente de la postulación")
    @GetMapping("/{id}/documentos")
    public ResponseEntity<List<DocumentoResponse>> documentos(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(documentoService.obtenerPorPostulacion(id, UsuarioActual.de(jwt)));
    }

    @Operation(summary = "Postularse a un proyecto disponible")
    @PreAuthorize("hasRole('ALUMNO')")
    @PostMapping
    public ResponseEntity<PostulacionResponse> crear(@Valid @RequestBody PostulacionRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postulacionService.crear(request, UsuarioActual.de(jwt)));
    }

    @Operation(summary = "Aceptar o rechazar una postulación en revisión")
    @PreAuthorize("hasAnyRole('EMPRESA', 'ADMIN')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PostulacionResponse> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoPostulacionRequest request,
                                                             @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postulacionService.cambiarEstado(id, request, UsuarioActual.de(jwt)));
    }

    @Operation(summary = "Asignar un asesor académico a una residencia en curso")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/asesor")
    public ResponseEntity<PostulacionResponse> asignarAsesor(@PathVariable Long id, @Valid @RequestBody AsignarAsesorRequest request) {
        return ResponseEntity.ok(postulacionService.asignarAsesor(id, request.idAsesor()));
    }

    @Operation(summary = "Finalizar la residencia (requiere el reporte final aprobado)")
    @PreAuthorize("hasAnyRole('ASESOR', 'ADMIN')")
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<PostulacionResponse> finalizar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postulacionService.finalizarResidencia(id, UsuarioActual.de(jwt)));
    }

    @Operation(summary = "Cancelar una postulación en revisión (alumno) o eliminarla (admin)")
    @PreAuthorize("hasAnyRole('ALUMNO', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        postulacionService.eliminar(id, UsuarioActual.de(jwt));
        return ResponseEntity.noContent().build();
    }
}
