package com.practica.webAvanzada.dto;

import java.util.List;

public record RecomendacionProyectosResponse(
        List<String> habilidadesReconocidas,
        List<String> habilidadesNoReconocidas,
        List<ProyectoRecomendado> proyectos
) {
    // afinidad va de 0 a 1
    public record ProyectoRecomendado(
            ProyectoResponse proyecto,
            double afinidad,
            List<String> habilidadesCoincidentes,
            List<String> habilidadesFaltantes
    ) {
    }
}
