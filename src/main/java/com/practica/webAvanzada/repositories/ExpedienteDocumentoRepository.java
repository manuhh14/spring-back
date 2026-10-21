package com.practica.webAvanzada.repositories;

import com.practica.webAvanzada.models.ExpedienteDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpedienteDocumentoRepository extends JpaRepository<ExpedienteDocumento, Long> {
}