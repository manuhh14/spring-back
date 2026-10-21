package com.practica.webAvanzada.controllers;

import com.practica.webAvanzada.models.ExpedienteDocumento;
import com.practica.webAvanzada.services.ExpedienteDocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class ExpedienteDocumentoController {

    private final ExpedienteDocumentoService documentoService;

    @GetMapping
    public ResponseEntity<List<ExpedienteDocumento>> listarTodos() {
        return ResponseEntity.ok(documentoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpedienteDocumento> obtenerPorId(@PathVariable Long id) {
        return documentoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ExpedienteDocumento> crear(@RequestBody ExpedienteDocumento documento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentoService.guardar(documento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpedienteDocumento> actualizar(@PathVariable Long id, @RequestBody ExpedienteDocumento documento) {
        ExpedienteDocumento documentoActualizado = documentoService.actualizar(id, documento);
        return documentoActualizado != null ? ResponseEntity.ok(documentoActualizado) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        documentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}