package com.practica.webAvanzada.exceptions;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Credenciales incorrectas");
    }
}
