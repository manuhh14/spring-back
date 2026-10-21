package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.models.ProyectoResidencia;
import com.practica.webAvanzada.services.ProyectoResidenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectoResidenciaController {

    private final ProyectoResidenciaService proyectoService;

    @GetMapping
    public ResponseEntity<List<ProyectoResidencia>> listarTodos() {
        return ResponseEntity.ok(proyectoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResidencia> obtenerPorId(@PathVariable Long id) {
        return proyectoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProyectoResidencia> crear(@RequestBody ProyectoResidencia proyecto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proyectoService.guardar(proyecto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProyectoResidencia> actualizar(@PathVariable Long id, @RequestBody ProyectoResidencia proyecto) {
        ProyectoResidencia proyectoActualizado = proyectoService.actualizar(id, proyecto);
        return proyectoActualizado != null ? ResponseEntity.ok(proyectoActualizado) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proyectoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}