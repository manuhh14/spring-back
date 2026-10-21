package com.practica.webAvanzada.services;

import com.practica.webAvanzada.models.Postulacion;
import com.practica.webAvanzada.repositories.PostulacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostulacionService {
    private final PostulacionRepository postulacionRepository;

    public Postulacion guardar(Postulacion postulacion) { return postulacionRepository.save(postulacion); }
    public List<Postulacion> obtenerTodos() { return postulacionRepository.findAll(); }
    public Optional<Postulacion> obtenerPorId(Long id) { return postulacionRepository.findById(id); }
    public void eliminar(Long id) { postulacionRepository.deleteById(id); }

    public Postulacion actualizar(Long id, Postulacion postulacionActualizada) {
        if (postulacionRepository.existsById(id)) {
            postulacionActualizada.setIdPostulacion(id);
            return postulacionRepository.save(postulacionActualizada);
        }
        return null;
    }
}