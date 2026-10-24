package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.dto.AlumnoResponse;
import com.practica.webAvanzada.dto.AlumnoUpdateRequest;
import com.practica.webAvanzada.security.UsuarioActual;
import com.practica.webAvanzada.services.AlumnoService;
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

// El alta de alumnos se hace en POST /api/auth/registro/alumno
@Tag(name = "Alumnos")
@RestController
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
public class AlumnoController {

    private final AlumnoService alumnoService;

    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    @GetMapping
    public ResponseEntity<Page<AlumnoResponse>> listarTodos(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(alumnoService.obtenerTodos(pageable));
    }

    @PreAuthorize("hasRole('ALUMNO')")
    @GetMapping("/me")
    public ResponseEntity<AlumnoResponse> obtenerPerfil(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(alumnoService.obtenerPerfil(UsuarioActual.de(jwt)));
    }

    @GetMapping("/{numeroControl}")
    public ResponseEntity<AlumnoResponse> obtenerPorId(@PathVariable String numeroControl, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(alumnoService.obtenerPorId(numeroControl, UsuarioActual.de(jwt)));
    }

    @PreAuthorize("hasAnyRole('ALUMNO', 'ADMIN')")
    @PutMapping("/{numeroControl}")
    public ResponseEntity<AlumnoResponse> actualizar(@PathVariable String numeroControl,
                                                     @Valid @RequestBody AlumnoUpdateRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(alumnoService.actualizar(numeroControl, request, UsuarioActual.de(jwt)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{numeroControl}")
    public ResponseEntity<Void> eliminar(@PathVariable String numeroControl) {
        alumnoService.eliminar(numeroControl);
        return ResponseEntity.noContent().build();
    }
}
