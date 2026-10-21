package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.models.Postulacion;
import com.practica.webAvanzada.services.PostulacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/postulaciones")
@RequiredArgsConstructor
public class PostulacionController {

    private final PostulacionService postulacionService;

    @GetMapping
    public ResponseEntity<List<Postulacion>> listarTodas() {
        return ResponseEntity.ok(postulacionService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Postulacion> obtenerPorId(@PathVariable Long id) {
        return postulacionService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Postulacion> crear(@RequestBody Postulacion postulacion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postulacionService.guardar(postulacion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Postulacion> actualizar(@PathVariable Long id, @RequestBody Postulacion postulacion) {
        Postulacion postulacionActualizada = postulacionService.actualizar(id, postulacion);
        return postulacionActualizada != null ? ResponseEntity.ok(postulacionActualizada) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        postulacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}