package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.dto.UsuarioRequest;
import com.practica.webAvanzada.dto.UsuarioResponse;
import com.practica.webAvanzada.dto.UsuarioUpdateRequest;
import com.practica.webAvanzada.security.UsuarioActual;
import com.practica.webAvanzada.services.UsuarioService;
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

@Tag(name = "Usuarios")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> listarTodos(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.obtenerTodos(pageable));
    }

    @Operation(summary = "Datos del usuario autenticado")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obtenerActual(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(usuarioService.obtenerActual(UsuarioActual.de(jwt)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @Operation(summary = "Crear un usuario sin perfil (p. ej. otro ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @Operation(summary = "Actualizar correo, contraseña (opcional) o activar/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
