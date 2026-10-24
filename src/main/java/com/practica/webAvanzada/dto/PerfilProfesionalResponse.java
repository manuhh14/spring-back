package com.practica.webAvanzada.dto;

import java.util.List;

public record PerfilProfesionalResponse(
        List<String> habilidadesReconocidas,
        List<String> habilidadesNoReconocidas,
        String perfil,
        List<String> habilidadesClave,
        List<String> habilidadesClaveFaltantes
) {
}
