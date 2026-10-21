package com.practica.webAvanzada.services;

import com.practica.webAvanzada.models.AsesorAcademico;
import com.practica.webAvanzada.repositories.AsesorAcademicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AsesorAcademicoService {
    private final AsesorAcademicoRepository asesorRepository;

    public AsesorAcademico guardar(AsesorAcademico asesor) { return asesorRepository.save(asesor); }
    public List<AsesorAcademico> obtenerTodos() { return asesorRepository.findAll(); }
    public Optional<AsesorAcademico> obtenerPorId(Long id) { return asesorRepository.findById(id); }
    public void eliminar(Long id) { asesorRepository.deleteById(id); }

    public AsesorAcademico actualizar(Long id, AsesorAcademico asesorActualizado) {
        if (asesorRepository.existsById(id)) {
            asesorActualizado.setIdAsesor(id);
            return asesorRepository.save(asesorActualizado);
        }
        return null;
    }
}