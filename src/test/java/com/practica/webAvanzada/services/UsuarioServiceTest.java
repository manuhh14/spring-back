package com.practica.webAvanzada.services;

import com.practica.webAvanzada.dto.LoginRequest;
import com.practica.webAvanzada.dto.UsuarioUpdateRequest;
import com.practica.webAvanzada.exceptions.CredencialesInvalidasException;
import com.practica.webAvanzada.exceptions.ReglaNegocioException;
import com.practica.webAvanzada.models.Usuario;
import com.practica.webAvanzada.models.enums.RoleUsuario;
import com.practica.webAvanzada.repositories.UsuarioRepository;
import com.practica.webAvanzada.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private JwtService jwtService;
    @Spy private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void actualizarCifraLaNuevaContrasena() {
        Usuario usuario = usuario("a@test.com", "vieja", true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.actualizar(1L, new UsuarioUpdateRequest("a@test.com", "NuevaClave1", true));

        assertThat(usuario.getPassword()).isNotEqualTo("NuevaClave1");
        assertThat(passwordEncoder.matches("NuevaClave1", usuario.getPassword())).isTrue();
    }

    @Test
    void actualizarSinContrasenaConservaLaActual() {
        Usuario usuario = usuario("a@test.com", "vieja", true);
        String hashOriginal = usuario.getPassword();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.actualizar(1L, new UsuarioUpdateRequest("a@test.com", null, false));

        assertThat(usuario.getPassword()).isEqualTo(hashOriginal);
        assertThat(usuario.getActivo()).isFalse();
    }

    @Test
    void crearRechazaCorreoDuplicado() {
        when(usuarioRepository.existsByCorreo("a@test.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crearUsuario("a@test.com", "Clave1234", RoleUsuario.ADMIN))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void loginRechazaUsuarioDesactivado() {
        Usuario desactivado = usuario("a@test.com", "Clave1234", false);
        when(usuarioRepository.findByCorreo("a@test.com")).thenReturn(Optional.of(desactivado));

        assertThatThrownBy(() -> usuarioService.login(new LoginRequest("a@test.com", "Clave1234")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void loginDevuelveTokenConCredencialesCorrectas() {
        Usuario activo = usuario("a@test.com", "Clave1234", true);
        when(usuarioRepository.findByCorreo("a@test.com")).thenReturn(Optional.of(activo));
        when(jwtService.generarToken(activo)).thenReturn("token");

        var respuesta = usuarioService.login(new LoginRequest("a@test.com", "Clave1234"));

        assertThat(respuesta.token()).isEqualTo("token");
        assertThat(respuesta.usuario().correo()).isEqualTo("a@test.com");
    }

    private Usuario usuario(String correo, String passwordPlano, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(passwordPlano));
        usuario.setRol(RoleUsuario.ALUMNO);
        usuario.setActivo(activo);
        return usuario;
    }
}
