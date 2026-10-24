package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.dto.AsesorRequest;
import com.practica.webAvanzada.dto.AsesorResponse;
import com.practica.webAvanzada.dto.AsesorUpdateRequest;
import com.practica.webAvanzada.dto.PostulacionResponse;
import com.practica.webAvanzada.security.UsuarioActual;
import com.practica.webAvanzada.services.AsesorAcademicoService;
import com.practica.webAvanzada.services.PostulacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Asesores académicos")
@RestController
@RequestMapping("/api/asesores")
@RequiredArgsConstructor
public class AsesorAcademicoController {

    private final AsesorAcademicoService asesorService;
    private final PostulacionService postulacionService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AsesorResponse>> listarTodos() {
        return ResponseEntity.ok(asesorService.obtenerTodos());
    }

    @PreAuthorize("hasRole('ASESOR')")
    @GetMapping("/me")
    public ResponseEntity<AsesorResponse> obtenerPerfil(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(asesorService.obtenerPerfil(UsuarioActual.de(jwt)));
    }

    @Operation(summary = "Residencias asignadas al asesor autenticado")
    @PreAuthorize("hasRole('ASESOR')")
    @GetMapping("/me/postulaciones")
    public ResponseEntity<List<PostulacionResponse>> misResidencias(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postulacionService.obtenerDeAsesorActual(UsuarioActual.de(jwt)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AsesorResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(asesorService.obtenerPorId(id));
    }

    @Operation(summary = "Dar de alta un asesor (crea su usuario y su perfil)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AsesorResponse> crear(@Valid @RequestBody AsesorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asesorService.crear(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AsesorResponse> actualizar(@PathVariable Long id, @Valid @RequestBody AsesorUpdateRequest request) {
        return ResponseEntity.ok(asesorService.actualizar(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        asesorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
