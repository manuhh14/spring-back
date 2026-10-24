package com.practica.webAvanzada.security;

import com.practica.webAvanzada.models.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    public static final String CLAIM_ID_USUARIO = "idUsuario";
    public static final String CLAIM_ROL = "rol";

    private final JwtEncoder jwtEncoder;
    private final long expiracionMinutos;

    public JwtService(JwtEncoder jwtEncoder, @Value("${app.jwt.expiracion-minutos}") long expiracionMinutos) {
        this.jwtEncoder = jwtEncoder;
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("webAvanzada")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES))
                .subject(usuario.getCorreo())
                .claim(CLAIM_ID_USUARIO, usuario.getIdUsuario())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpiracionSegundos() {
        return expiracionMinutos * 60;
    }
}
