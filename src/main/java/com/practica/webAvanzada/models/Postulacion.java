package com.practica.webAvanzada.models;

import com.practica.webAvanzada.models.enums.EstadoPostulacion;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "postulaciones")
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPostulacion;

    // Relación Muchos a Uno con Alumno (La llave foránea es numero_control)
    @ManyToOne
    @JoinColumn(name = "numero_control", referencedColumnName = "numeroControl", nullable = false)
    private Alumno alumno;

    // Relación Muchos a Uno con ProyectoResidencia
    @ManyToOne
    @JoinColumn(name = "id_proyecto", nullable = false)
    private ProyectoResidencia proyecto;

    private LocalDate fechaSolicitud;

    @Enumerated(EnumType.STRING)
    private EstadoPostulacion estado;

    @Column(columnDefinition = "TEXT")
    private String comentariosEmpresa;
}