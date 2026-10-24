package com.practica.webAvanzada.repositories;

import com.practica.webAvanzada.models.Postulacion;
import com.practica.webAvanzada.models.enums.EstadoPostulacion;
import com.practica.webAvanzada.models.enums.EstadoResidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    List<Postulacion> findByAlumnoNumeroControl(String numeroControl);

    List<Postulacion> findByAlumnoNumeroControlAndEstado(String numeroControl, EstadoPostulacion estado);

    List<Postulacion> findByProyectoIdProyecto(Long idProyecto);

    List<Postulacion> findByAsesorIdAsesor(Long idAsesor);

    boolean existsByAlumnoNumeroControlAndProyectoIdProyecto(String numeroControl, Long idProyecto);

    boolean existsByAlumnoNumeroControl(String numeroControl);

    boolean existsByProyectoIdProyecto(Long idProyecto);

    boolean existsByAsesorIdAsesor(Long idAsesor);

    long countByProyectoIdProyectoAndEstado(Long idProyecto, EstadoPostulacion estado);

    // Alumnos que el asesor tiene actualmente en seguimiento (residencia en curso)
    long countByAsesorIdAsesorAndAlumnoEstadoResidencia(Long idAsesor, EstadoResidencia estadoResidencia);
}
