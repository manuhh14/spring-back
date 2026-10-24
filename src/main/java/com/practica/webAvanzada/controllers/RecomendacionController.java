package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.dto.CatalogoHabilidadesResponse;
import com.practica.webAvanzada.dto.PerfilProfesionalResponse;
import com.practica.webAvanzada.dto.RecomendacionProyectosResponse;
import com.practica.webAvanzada.dto.SugerenciaHabilidadesResponse;
import com.practica.webAvanzada.security.UsuarioActual;
import com.practica.webAvanzada.services.RecomendacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recomendaciones")
@RestController
@RequestMapping("/api/recomendaciones")
@RequiredArgsConstructor
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    @Operation(summary = "Proyectos disponibles ordenados por afinidad con las habilidades del alumno autenticado")
    @PreAuthorize("hasRole('ALUMNO')")
    @GetMapping("/proyectos")
    public ResponseEntity<RecomendacionProyectosResponse> proyectos(@RequestParam(defaultValue = "10") @Min(1) @Max(50) int limite,
                                                                    @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(recomendacionService.recomendarProyectos(UsuarioActual.de(jwt), limite));
    }

    @Operation(summary = "Habilidades que le conviene aprender al alumno autenticado")
    @PreAuthorize("hasRole('ALUMNO')")
    @GetMapping("/habilidades")
    public ResponseEntity<SugerenciaHabilidadesResponse> habilidades(@RequestParam(defaultValue = "10") @Min(1) @Max(50) int limite,
                                                                     @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(recomendacionService.sugerirHabilidades(UsuarioActual.de(jwt), limite));
    }

    @Operation(summary = "Perfil profesional del alumno autenticado (Backend Java, Frontend web...)")
    @PreAuthorize("hasRole('ALUMNO')")
    @GetMapping("/perfil")
    public ResponseEntity<PerfilProfesionalResponse> perfil(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(recomendacionService.obtenerPerfil(UsuarioActual.de(jwt)));
    }

    @Operation(summary = "Catálogo de habilidades reconocidas, para autocompletar formularios")
    @GetMapping("/catalogo-habilidades")
    public ResponseEntity<CatalogoHabilidadesResponse> catalogo(@RequestParam(required = false) String categoria,
                                                                @RequestParam(required = false) String busqueda) {
        return ResponseEntity.ok(recomendacionService.obtenerCatalogo(categoria, busqueda));
    }
}
