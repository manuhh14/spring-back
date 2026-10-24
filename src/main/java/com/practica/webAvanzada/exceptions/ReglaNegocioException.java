package com.practica.webAvanzada.exceptions;

// Operación que viola una regla del dominio (cupo lleno, estado inválido, duplicado...). Se traduce a 409
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
