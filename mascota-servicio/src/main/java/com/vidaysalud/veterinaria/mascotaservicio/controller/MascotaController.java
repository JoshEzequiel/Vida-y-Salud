package com.vidaysalud.veterinaria.mascotaservicio.controller;

import com.vidaysalud.veterinaria.mascotaservicio.dto.ApiResponse;
import com.vidaysalud.veterinaria.mascotaservicio.dto.MascotaRequestDTO;
import com.vidaysalud.veterinaria.mascotaservicio.dto.MascotaResponseDTO;
import com.vidaysalud.veterinaria.mascotaservicio.service.MascotaService;
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
        name = "Mascotas",
        description = "Endpoints para gestionar mascotas y relacionarlas con clientes"
)
@RestController
@RequestMapping("/api/v1/mascotas")
@RequiredArgsConstructor
public class MascotaController {

 private final MascotaService service;

 @Operation(
         summary = "Listar mascotas",
         description = "Obtiene todas las mascotas registradas en el sistema."
 )
 @ApiResponses(value = {
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mascotas obtenidas correctamente"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor")
 })
 @GetMapping
 public ResponseEntity<ApiResponse<List<MascotaResponseDTO>>> listar() {
  return ok("Mascotas obtenidas", service.listar());
 }

 @Operation(
         summary = "Buscar mascota por ID",
         description = "Obtiene la información de una mascota específica según su identificador."
 )
 @ApiResponses(value = {
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mascota encontrada"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Mascota no encontrada")
 })
 @GetMapping("/{id}")
 public ResponseEntity<ApiResponse<MascotaResponseDTO>> buscar(@PathVariable Integer id) {
  return ok("Mascota encontrada", service.buscar(id));
 }

 @Operation(
         summary = "Listar mascotas por cliente",
         description = "Obtiene todas las mascotas asociadas a un cliente específico."
 )
 @ApiResponses(value = {
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mascotas del cliente obtenidas correctamente"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Servicio de clientes no disponible")
 })
 @GetMapping("/cliente/{idCliente}")
 public ResponseEntity<ApiResponse<List<MascotaResponseDTO>>> cliente(
         @PathVariable Integer idCliente
 ) {
  return ok("Mascotas del cliente", service.porCliente(idCliente));
 }

 @Operation(
         summary = "Crear mascota",
         description = "Registra una nueva mascota asociada a un cliente existente."
 )
 @ApiResponses(value = {
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Mascota creada correctamente"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente asociado no encontrado"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Servicio de clientes no disponible")
 })
 @PostMapping
 public ResponseEntity<ApiResponse<MascotaResponseDTO>> crear(
         @Valid @RequestBody MascotaRequestDTO d
 ) {
  return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(resp(HttpStatus.CREATED, "Mascota creada", service.crear(d)));
 }

 @Operation(
         summary = "Actualizar mascota",
         description = "Actualiza los datos de una mascota existente."
 )
 @ApiResponses(value = {
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mascota actualizada correctamente"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Mascota no encontrada")
 })
 @PutMapping("/{id}")
 public ResponseEntity<ApiResponse<MascotaResponseDTO>> actualizar(
         @PathVariable Integer id,
         @Valid @RequestBody MascotaRequestDTO d
 ) {
  return ok("Mascota actualizada", service.actualizar(id, d));
 }

 @Operation(
         summary = "Eliminar mascota",
         description = "Elimina una mascota existente según su ID."
 )
 @ApiResponses(value = {
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mascota eliminada correctamente"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Mascota no encontrada")
 })
 @DeleteMapping("/{id}")
 public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
  service.eliminar(id);
  return ok("Mascota eliminada", null);
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

