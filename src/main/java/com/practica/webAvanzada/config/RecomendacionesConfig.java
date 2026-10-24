package com.practica.webAvanzada.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RecomendacionesConfig {

    @Bean
    public RestClient recomendacionesRestClient(@Value("${app.recomendaciones.url}") String url,
                                                @Value("${app.recomendaciones.api-key}") String apiKey,
                                                @Value("${app.recomendaciones.timeout-segundos}") long timeoutSegundos) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(timeoutSegundos));
        fabrica.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(url)
                .requestFactory(fabrica);
        // Sin clave configurada no se envía el header (el microservicio la exige solo si define API_KEY)
        if (!apiKey.isBlank()) {
            builder.defaultHeader("X-API-Key", apiKey);
        }
        return builder.build();
    }
}
