package com.practica.webAvanzada.exceptions;

// Un servicio externo (p. ej. el microservicio de recomendaciones) no responde o falla. Se traduce a 503
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
