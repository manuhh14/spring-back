package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.dto.*;
import com.practica.webAvanzada.services.AlumnoService;
import com.practica.webAvanzada.services.EmpresaService;
import com.practica.webAvanzada.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación")
@SecurityRequirements
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final AlumnoService alumnoService;
    private final EmpresaService empresaService;

    @Operation(summary = "Iniciar sesión y obtener un token JWT")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @Operation(summary = "Registro de un alumno (crea su usuario y su perfil)")
    @PostMapping("/registro/alumno")
    public ResponseEntity<AlumnoResponse> registrarAlumno(@Valid @RequestBody RegistroAlumnoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alumnoService.registrar(request));
    }

    @Operation(summary = "Registro de una empresa (queda pendiente de verificación por un administrador)")
    @PostMapping("/registro/empresa")
    public ResponseEntity<EmpresaResponse> registrarEmpresa(@Valid @RequestBody RegistroEmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.registrar(request));
    }
}
