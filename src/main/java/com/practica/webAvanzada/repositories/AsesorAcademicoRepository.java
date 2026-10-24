package com.practica.webAvanzada.repositories;

import com.practica.webAvanzada.models.AsesorAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsesorAcademicoRepository extends JpaRepository<AsesorAcademico, Long> {

    Optional<AsesorAcademico> findByUsuarioIdUsuario(Long idUsuario);
}
