package com.practica.webAvanzada.repositories;

import com.practica.webAvanzada.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring Data crea automáticamente el query SQL con solo leer el nombre
    Optional<Usuario> findByCorreo(String correo);
}