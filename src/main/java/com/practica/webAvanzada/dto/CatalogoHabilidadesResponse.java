package com.practica.webAvanzada.dto;

import java.util.List;

public record CatalogoHabilidadesResponse(int total, List<Habilidad> habilidades) {

    public record Habilidad(String habilidad, String categoria) {
    }
}
