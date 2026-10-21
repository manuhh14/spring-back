package com.practica.webAvanzada.services;

import com.practica.webAvanzada.models.ProyectoResidencia;
import com.practica.webAvanzada.repositories.ProyectoResidenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProyectoResidenciaService {
    private final ProyectoResidenciaRepository proyectoRepository;

    public ProyectoResidencia guardar(ProyectoResidencia proyecto) { return proyectoRepository.save(proyecto); }
    public List<ProyectoResidencia> obtenerTodos() { return proyectoRepository.findAll(); }
    public Optional<ProyectoResidencia> obtenerPorId(Long id) { return proyectoRepository.findById(id); }
    public void eliminar(Long id) { proyectoRepository.deleteById(id); }

    public ProyectoResidencia actualizar(Long id, ProyectoResidencia proyectoActualizado) {
        if (proyectoRepository.existsById(id)) {
            proyectoActualizado.setIdProyecto(id);
            return proyectoRepository.save(proyectoActualizado);
        }
        return null;
    }
}