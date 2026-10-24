package com.practica.webAvanzada.models;

import com.practica.webAvanzada.models.enums.EstadoResidencia;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "alumnos")
public class Alumno {

    @Id
    private String numeroControl;

    @OneToOne
    @JoinColumn(name = "id_usuario",referencedColumnName = "idUsuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false)
    private String carrera;

    @Column(nullable = false)
    private int semestre;

    @Column(name = "habilidadestecnicas", columnDefinition = "TEXT")
    private String habilidadesTecnicas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoResidencia estadoResidencia = EstadoResidencia.PENDIENTE;
}
