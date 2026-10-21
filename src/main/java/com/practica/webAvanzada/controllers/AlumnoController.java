package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.models.Alumno;
import com.practica.webAvanzada.services.AlumnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
public class AlumnoController {

    private final AlumnoService alumnoService;

    @GetMapping
    public ResponseEntity<List<Alumno>> listarTodos() {
        return ResponseEntity.ok(alumnoService.obtenerTodos());
    }

    @GetMapping("/{numeroControl}")
    public ResponseEntity<Alumno> obtenerPorId(@PathVariable String numeroControl) {
        return alumnoService.obtenerPorId(numeroControl)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Alumno> crear(@RequestBody Alumno alumno) {
        Alumno nuevoAlumno = alumnoService.guardar(alumno);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAlumno);
    }

    @PutMapping("/{numeroControl}")
    public ResponseEntity<Alumno> actualizar(@PathVariable String numeroControl, @RequestBody Alumno alumno) {
        Alumno alumnoActualizado = alumnoService.actualizar(numeroControl, alumno);
        if (alumnoActualizado != null) {
            return ResponseEntity.ok(alumnoActualizado);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{numeroControl}")
    public ResponseEntity<Void> eliminar(@PathVariable String numeroControl) {
        alumnoService.eliminar(numeroControl);
        return ResponseEntity.noContent().build();
    }
}