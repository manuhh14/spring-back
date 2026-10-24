package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.dto.PostulacionResponse;
import com.practica.webAvanzada.dto.ProyectoRequest;
import com.practica.webAvanzada.dto.ProyectoResponse;
import com.practica.webAvanzada.models.enums.EstadoProyecto;
import com.practica.webAvanzada.security.UsuarioActual;
import com.practica.webAvanzada.services.PostulacionService;
import com.practica.webAvanzada.services.ProyectoResidenciaService;
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

@Tag(name = "Proyectos de residencia")
@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectoResidenciaController {

    private final ProyectoResidenciaService proyectoService;
    private final PostulacionService postulacionService;

    @Operation(summary = "Listar proyectos, opcionalmente filtrados por estado (p. ej. DISPONIBLE)")
    @GetMapping
    public ResponseEntity<Page<ProyectoResponse>> listarTodos(@RequestParam(required = false) EstadoProyecto estado,
                                                              @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(proyectoService.obtenerTodos(estado, pageable));
    }

    @PreAuthorize("hasRole('EMPRESA')")
    @GetMapping("/mis-proyectos")
    public ResponseEntity<List<ProyectoResponse>> misProyectos(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(proyectoService.obtenerDeEmpresaActual(UsuarioActual.de(jwt)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proyectoService.obtenerPorId(id));
    }

    @Operation(summary = "Postulaciones recibidas por un proyecto")
    @PreAuthorize("hasAnyRole('EMPRESA', 'ADMIN')")
    @GetMapping("/{id}/postulaciones")
    public ResponseEntity<List<PostulacionResponse>> postulaciones(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postulacionService.obtenerPorProyecto(id, UsuarioActual.de(jwt)));
    }

    @Operation(summary = "Publicar un proyecto (la empresa debe estar verificada)")
    @PreAuthorize("hasRole('EMPRESA')")
    @PostMapping
    public ResponseEntity<ProyectoResponse> crear(@Valid @RequestBody ProyectoRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proyectoService.crear(request, UsuarioActual.de(jwt)));
    }

    @PreAuthorize("hasAnyRole('EMPRESA', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProyectoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ProyectoRequest request,
                                                       @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(proyectoService.actualizar(id, request, UsuarioActual.de(jwt)));
    }

    @Operation(summary = "Cerrar el proyecto (rechaza las postulaciones pendientes)")
    @PreAuthorize("hasAnyRole('EMPRESA', 'ADMIN')")
    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<ProyectoResponse> cerrar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(proyectoService.cerrar(id, UsuarioActual.de(jwt)));
    }

    @PreAuthorize("hasAnyRole('EMPRESA', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        proyectoService.eliminar(id, UsuarioActual.de(jwt));
        return ResponseEntity.noContent().build();
    }
}
