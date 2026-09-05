package com.practica.webAvanzada.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmpresa;

    @OneToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "idUsuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private String razonSocial;

    @Column(length = 100)
    private String sector;

    @Column(nullable = false)
    private String nombreContacto;

    @Column(length = 20)
    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Column(nullable = false)
    private Boolean estadoVerificacion = false;
}