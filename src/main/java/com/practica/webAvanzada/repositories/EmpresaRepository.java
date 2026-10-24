package com.practica.webAvanzada.repositories;

import com.practica.webAvanzada.models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByUsuarioIdUsuario(Long idUsuario);
}
