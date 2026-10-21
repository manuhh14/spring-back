package com.practica.webAvanzada.services;

import com.practica.webAvanzada.models.ExpedienteDocumento;
import com.practica.webAvanzada.repositories.ExpedienteDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpedienteDocumentoService {
    private final ExpedienteDocumentoRepository documentoRepository;

    public ExpedienteDocumento guardar(ExpedienteDocumento documento) { return documentoRepository.save(documento); }
    public List<ExpedienteDocumento> obtenerTodos() { return documentoRepository.findAll(); }
    public Optional<ExpedienteDocumento> obtenerPorId(Long id) { return documentoRepository.findById(id); }
    public void eliminar(Long id) { documentoRepository.deleteById(id); }

    public ExpedienteDocumento actualizar(Long id, ExpedienteDocumento documentoActualizado) {
        if (documentoRepository.existsById(id)) {
            documentoActualizado.setIdDocumento(id);
            return documentoRepository.save(documentoActualizado);
        }
        return null;
    }
}