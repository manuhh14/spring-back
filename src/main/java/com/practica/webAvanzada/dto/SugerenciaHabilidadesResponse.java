package com.practica.webAvanzada.dto;

import java.util.List;

// relevancia va de 0 a 1
public record SugerenciaHabilidadesResponse(
        List<String> habilidadesReconocidas,
        List<String> habilidadesNoReconocidas,
        List<HabilidadSugerida> sugerencias
) {
    public record HabilidadSugerida(String habilidad, String categoria, double relevancia) {
    }
}
