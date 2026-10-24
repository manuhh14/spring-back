package com.practica.webAvanzada.exceptions;

// Datos que pasan la validación de formato pero no son aceptables (archivo vacío, tipo no permitido...). Se traduce a 400
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
