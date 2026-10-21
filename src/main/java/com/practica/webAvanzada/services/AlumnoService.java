package com.practica.webAvanzada.services;

import com.practica.webAvanzada.models.Alumno;
import com.practica.webAvanzada.repositories.AlumnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlumnoService {
    private final AlumnoRepository alumnoRepository;

    public Alumno guardar(Alumno alumno) { return alumnoRepository.save(alumno); }
    public List<Alumno> obtenerTodos() { return alumnoRepository.findAll(); }
    public Optional<Alumno> obtenerPorId(String id) { return alumnoRepository.findById(id); }
    public void eliminar(String id) { alumnoRepository.deleteById(id); }

    public Alumno actualizar(String numeroControl, Alumno alumnoActualizado) {
        if (alumnoRepository.existsById(numeroControl)) {
            alumnoActualizado.setNumeroControl(numeroControl);
            return alumnoRepository.save(alumnoActualizado);
        }
        return null;
    }
}