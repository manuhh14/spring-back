package com.practica.webAvanzada.repositories;

import com.practica.webAvanzada.models.ProyectoResidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProyectoResidenciaRepository extends JpaRepository<ProyectoResidencia, Long> {
}