package com.vidaysalud.veterinaria.clienteservicio.controller;

import com.vidaysalud.veterinaria.clienteservicio.dto.ApiResponse;
import com.vidaysalud.veterinaria.clienteservicio.dto.ClienteRequestDTO;
import com.vidaysalud.veterinaria.clienteservicio.dto.ClienteResponseDTO;
import com.vidaysalud.veterinaria.clienteservicio.service.ClienteService;
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
        name = "Clientes",
        description = "Endpoints para gestionar clientes de la veterinaria"
)
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

 private final ClienteService service;

 @Operation(
         summary = "Listar clientes",
         description = "Obtiene todos los clientes registrados en el sistema."
 )
 @ApiResponses(value = {
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Clientes obtenidos correctamente"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor")
 })
 @GetMapping
 public ResponseEntity<ApiResponse<List<ClienteResponseDTO>>> listar() {
  return ok("Clientes obtenidos", service.listar());
 }

 @Operation(
         summary = "Buscar cliente por ID",
         description = "Obtiene la información de un cliente específico según su identificador."
 )
 @ApiResponses(value = {
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente encontrado"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado")
 })
 @GetMapping("/{id}")
 public ResponseEntity<ApiResponse<ClienteResponseDTO>> id(@PathVariable Integer id) {
  return ok("Cliente encontrado", service.buscarPorId(id));
 }

 @Operation(
         summary = "Buscar cliente por RUT",
         description = "Obtiene la información de un cliente según su RUT."
 )
 @ApiResponses(value = {
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente encontrado"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado")
 })
 @GetMapping("/rut/{rut}")
 public ResponseEntity<ApiResponse<ClienteResponseDTO>> rut(@PathVariable String rut) {
  return ok("Cliente encontrado", service.buscarPorRut(rut));
 }

 @Operation(
         summary = "Crear cliente",
         description = "Registra un nuevo cliente validando datos obligatorios, RUT y correo."
 )
 @ApiResponses(value = {
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Cliente creado correctamente"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "RUT o correo ya registrado")
 })
 @PostMapping
 public ResponseEntity<ApiResponse<ClienteResponseDTO>> crear(
         @Valid @RequestBody ClienteRequestDTO d
 ) {
  return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(resp(HttpStatus.CREATED, "Cliente creado", service.crear(d)));
 }

 @Operation(
         summary = "Actualizar cliente",
         description = "Actualiza los datos de un cliente existente."
 )
 @ApiResponses(value = {
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente actualizado correctamente"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "RUT o correo duplicado")
 })
 @PutMapping("/{id}")
 public ResponseEntity<ApiResponse<ClienteResponseDTO>> actualizar(
         @PathVariable Integer id,
         @Valid @RequestBody ClienteRequestDTO d
 ) {
  return ok("Cliente actualizado", service.actualizar(id, d));
 }

 @Operation(
         summary = "Eliminar cliente",
         description = "Elimina un cliente existente según su ID."
 )
 @ApiResponses(value = {
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente eliminado correctamente"),
         @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado")
 })
 @DeleteMapping("/{id}")
 public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
  service.eliminar(id);
  return ok("Cliente eliminado", null);
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