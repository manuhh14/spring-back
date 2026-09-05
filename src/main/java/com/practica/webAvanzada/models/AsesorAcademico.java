package com.practica.webAvanzada.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "asesores_academicos")
public class AsesorAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAsesor;

    @OneToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "idUsuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private String nombreCompleto;

    private String departamento;

    private Integer maximoAlumnos;
}