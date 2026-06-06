package com.vidaysalud.veterinaria.inventarioservicio.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private int status;
    private String mensaje;
    private T data;
}
