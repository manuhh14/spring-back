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

    @ManyToOne
    @JoinColumn(name = "numero_control", referencedColumnName = "numeroControl", nullable = false)
    private Alumno alumno;

    @ManyToOne
    @JoinColumn(name = "id_proyecto", nullable = false)
    private ProyectoResidencia proyecto;

    // Asesor académico que da seguimiento a la residencia (se asigna al aceptar la postulación)
    @ManyToOne
    @JoinColumn(name = "id_asesor")
    private AsesorAcademico asesor;

    @Column(nullable = false)
    private LocalDate fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPostulacion estado;

    @Column(columnDefinition = "TEXT")
    private String comentariosEmpresa;
}