package com.practica.webAvanzada.exceptions;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " con id '" + id + "' no encontrado");
    }
}
