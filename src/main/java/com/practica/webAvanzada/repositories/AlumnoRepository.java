package com.practica.webAvanzada.repositories;

import com.practica.webAvanzada.models.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, String> {

    Optional<Alumno> findByUsuarioIdUsuario(Long idUsuario);
}
