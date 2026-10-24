package com.practica.webAvanzada.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorResponse> solicitudInvalida(SolicitudInvalidaException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> credenciales(CredencialesInvalidasException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> servicioNoDisponible(ServicioNoDisponibleException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request, null);
    }

    // Tanto los permisos por rol (@PreAuthorize) como las comprobaciones de propiedad en los servicios
    @ExceptionHandler({AccesoDenegadoException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> accesoDenegado(RuntimeException ex, HttpServletRequest request) {
        String mensaje = ex instanceof AccesoDenegadoException ? ex.getMessage() : "No tienes permiso para realizar esta acción";
        return respuesta(HttpStatus.FORBIDDEN, mensaje, request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "La petición contiene datos inválidos", request, errores);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, MissingServletRequestPartException.class,
            HandlerMethodValidationException.class})
    public ResponseEntity<ErrorResponse> peticionMalFormada(Exception ex, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "Petición mal formada: revisa el cuerpo y los parámetros", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return respuesta(HttpStatus.CONFLICT, "La operación viola una restricción de la base de datos (registro duplicado o con dependencias)", request, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> archivoGrande(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.CONTENT_TOO_LARGE, "El archivo supera el tamaño máximo permitido", request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.NOT_FOUND, "Ruta no encontrada", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> general(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request, null);
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje, HttpServletRequest request,
                                                    Map<String, String> errores) {
        ErrorResponse cuerpo = new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensaje, request.getRequestURI(), errores);
        return ResponseEntity.status(status).body(cuerpo);
    }
}
