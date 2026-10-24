package com.practica.webAvanzada.repositories;

import com.practica.webAvanzada.models.ExpedienteDocumento;
import com.practica.webAvanzada.models.enums.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpedienteDocumentoRepository extends JpaRepository<ExpedienteDocumento, Long> {

    List<ExpedienteDocumento> findByPostulacionIdPostulacion(Long idPostulacion);

    boolean existsByPostulacionIdPostulacion(Long idPostulacion);

    boolean existsByPostulacionIdPostulacionAndTipoDocumentoAndEstadoAprobacionTrue(Long idPostulacion, TipoDocumento tipoDocumento);
}
