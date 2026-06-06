package com.vidaysalud.veterinaria.recetaservicio.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> noEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest req
    ) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), req, null, null);
    }

    @ExceptionHandler(DatoDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> duplicado(
            DatoDuplicadoException ex,
            HttpServletRequest req
    ) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), req, null, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiErrorResponse> regla(
            ReglaNegocioException ex,
            HttpServletRequest req
    ) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null, null);
    }

    @ExceptionHandler(ServicioRemotoException.class)
    public ResponseEntity<ApiErrorResponse> remoto(
            ServicioRemotoException ex,
            HttpServletRequest req
    ) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req, null, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
    ) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        return respuesta(
                HttpStatus.BAD_REQUEST,
                "Los datos enviados contienen errores",
                req,
                errores,
                null
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> tipo(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest req
    ) {
        return respuesta(
                HttpStatus.BAD_REQUEST,
                "Parámetro inválido: " + ex.getName(),
                req,
                null,
                null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> integridad(
            DataIntegrityViolationException ex,
            HttpServletRequest req
    ) {
        return respuesta(
                HttpStatus.CONFLICT,
                "La operación afecta la integridad de los datos",
                req,
                null,
                ex
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> general(
            Exception ex,
            HttpServletRequest req
    ) {
        return respuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno",
                req,
                null,
                ex
        );
    }

    private ResponseEntity<ApiErrorResponse> respuesta(
            HttpStatus estado,
            String mensaje,
            HttpServletRequest req,
            Map<String, String> errores,
            Throwable causa
    ) {
        if (causa != null) {
            log.error(
                    "{} {}: {}",
                    req.getMethod(),
                    req.getRequestURI(),
                    mensaje,
                    causa
            );
        } else {
            log.warn(
                    "{} {}: {}",
                    req.getMethod(),
                    req.getRequestURI(),
                    mensaje
            );
        }

        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(estado.value())
                .error(estado.getReasonPhrase())
                .mensaje(mensaje)
                .ruta(req.getRequestURI())
                .erroresValidacion(errores)
                .build();

        return ResponseEntity.status(estado).body(body);
    }
}
