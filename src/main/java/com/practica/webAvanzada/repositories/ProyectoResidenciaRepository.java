package com.practica.webAvanzada.repositories;

import com.practica.webAvanzada.models.ProyectoResidencia;
import com.practica.webAvanzada.models.enums.EstadoProyecto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProyectoResidenciaRepository extends JpaRepository<ProyectoResidencia, Long> {

    Page<ProyectoResidencia> findByEstado(EstadoProyecto estado, Pageable pageable);

    List<ProyectoResidencia> findByEmpresaIdEmpresa(Long idEmpresa);

    boolean existsByEmpresaIdEmpresa(Long idEmpresa);
}
