package com.practica.webAvanzada.config;

import com.practica.webAvanzada.models.enums.RoleUsuario;
import com.practica.webAvanzada.repositories.UsuarioRepository;
import com.practica.webAvanzada.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Crea el primer administrador para poder verificar empresas y dar de alta asesores
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Value("${app.admin.correo}")
    private String correoAdmin;

    @Value("${app.admin.password:}")
    private String passwordAdmin;

    @Override
    public void run(String... args) {
        if (usuarioRepository.existsByRol(RoleUsuario.ADMIN)) {
            return;
        }
        if (passwordAdmin.isBlank()) {
            log.warn("No existe ningún ADMIN y ADMIN_PASSWORD no está definido: no se creó el administrador inicial");
            return;
        }
        usuarioService.crearUsuario(correoAdmin, passwordAdmin, RoleUsuario.ADMIN);
        log.info("Administrador inicial creado: {}", correoAdmin);
    }
}
