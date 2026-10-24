package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.dto.EmpresaResponse;
import com.practica.webAvanzada.dto.EmpresaUpdateRequest;
import com.practica.webAvanzada.dto.VerificacionEmpresaRequest;
import com.practica.webAvanzada.security.UsuarioActual;
import com.practica.webAvanzada.services.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

// El alta de empresas se hace en POST /api/auth/registro/empresa
@Tag(name = "Empresas")
@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    public ResponseEntity<Page<EmpresaResponse>> listarTodas(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(empresaService.obtenerTodos(pageable));
    }

    @PreAuthorize("hasRole('EMPRESA')")
    @GetMapping("/me")
    public ResponseEntity<EmpresaResponse> obtenerPerfil(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(empresaService.obtenerPerfil(UsuarioActual.de(jwt)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.obtenerPorId(id));
    }

    @PreAuthorize("hasAnyRole('EMPRESA', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EmpresaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody EmpresaUpdateRequest request,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(empresaService.actualizar(id, request, UsuarioActual.de(jwt)));
    }

    @Operation(summary = "Verificar (o retirar la verificación) de una empresa")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/verificacion")
    public ResponseEntity<EmpresaResponse> cambiarVerificacion(@PathVariable Long id, @Valid @RequestBody VerificacionEmpresaRequest request) {
        return ResponseEntity.ok(empresaService.cambiarVerificacion(id, request.verificada()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empresaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
