package com.vidaysalud.veterinaria.citaservicio.controller;

import com.vidaysalud.veterinaria.citaservicio.dto.ApiResponse;
import com.vidaysalud.veterinaria.citaservicio.dto.CitaRequestDTO;
import com.vidaysalud.veterinaria.citaservicio.dto.CitaResponseDTO;
import com.vidaysalud.veterinaria.citaservicio.dto.EstadoCitaRequestDTO;
import com.vidaysalud.veterinaria.citaservicio.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(
        name = "Citas",
        description = "Endpoints para agendar y gestionar citas veterinarias"
)
@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService service;

    @Operation(
            summary = "Listar citas",
            description = "Obtiene todas las citas registradas en el sistema."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Citas obtenidas correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CitaResponseDTO>>> listar() {
        return ok("Citas obtenidas", service.listar());
    }

    @Operation(
            summary = "Buscar cita por ID",
            description = "Obtiene la información de una cita específica según su identificador."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cita encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CitaResponseDTO>> buscar(@PathVariable Integer id) {
        return ok("Cita encontrada", service.buscar(id));
    }

    @Operation(
            summary = "Listar citas por mascota",
            description = "Obtiene todas las citas asociadas a una mascota específica."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Citas de mascota obtenidas correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Mascota no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Servicio de mascotas no disponible")
    })
    @GetMapping("/mascota/{id}")
    public ResponseEntity<ApiResponse<List<CitaResponseDTO>>> porMascota(@PathVariable Integer id) {
        return ok("Citas de mascota", service.porMascota(id));
    }

    @Operation(
            summary = "Crear cita",
            description = "Agenda una nueva cita validando la existencia de la mascota y del veterinario."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Cita creada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Mascota o veterinario no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Servicio remoto no disponible")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CitaResponseDTO>> crear(
            @Valid @RequestBody CitaRequestDTO d
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resp(HttpStatus.CREATED, "Cita creada", service.crear(d)));
    }

    @Operation(
            summary = "Actualizar cita",
            description = "Actualiza los datos de una cita existente."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cita actualizada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CitaResponseDTO>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody CitaRequestDTO d
    ) {
        return ok("Cita actualizada", service.actualizar(id, d));
    }

    @Operation(
            summary = "Actualizar estado de cita",
            description = "Actualiza únicamente el estado de una cita, por ejemplo: Pendiente, Confirmada, Atendida o Cancelada."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Estado inválido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<CitaResponseDTO>> actualizarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody EstadoCitaRequestDTO d
    ) {
        return ok("Estado actualizado", service.estado(id, d.getEstado()));
    }

    @Operation(
            summary = "Eliminar cita",
            description = "Elimina una cita existente según su ID."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cita eliminada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ok("Cita eliminada", null);
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String mensaje, T data) {
        return ResponseEntity.ok(resp(HttpStatus.OK, mensaje, data));
    }

    private <T> ApiResponse<T> resp(HttpStatus status, String mensaje, T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .mensaje(mensaje)
                .data(data)
                .build();
    }
}
