package com.vidaysalud.veterinaria.mascotaservicio.controller;
import com.vidaysalud.veterinaria.mascotaservicio.dto.*; import com.vidaysalud.veterinaria.mascotaservicio.service.MascotaService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.time.LocalDateTime; import java.util.List;
@RestController @RequestMapping("/api/v1/mascotas") @RequiredArgsConstructor
public class MascotaController{private final MascotaService service;
 @GetMapping public ResponseEntity<ApiResponse<List<MascotaResponseDTO>>> listar(){return ok("Mascotas obtenidas",service.listar());}
 @GetMapping("/{id}") public ResponseEntity<ApiResponse<MascotaResponseDTO>> buscar(@PathVariable Integer id){return ok("Mascota encontrada",service.buscar(id));}
 @GetMapping("/cliente/{idCliente}") public ResponseEntity<ApiResponse<List<MascotaResponseDTO>>> cliente(@PathVariable Integer idCliente){return ok("Mascotas del cliente",service.porCliente(idCliente));}
 @PostMapping public ResponseEntity<ApiResponse<MascotaResponseDTO>> crear(@Valid @RequestBody MascotaRequestDTO d){return ResponseEntity.status(201).body(resp(HttpStatus.CREATED,"Mascota creada",service.crear(d)));}
 @PutMapping("/{id}") public ResponseEntity<ApiResponse<MascotaResponseDTO>> actualizar(@PathVariable Integer id,@Valid @RequestBody MascotaRequestDTO d){return ok("Mascota actualizada",service.actualizar(id,d));}
 @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id){service.eliminar(id);return ok("Mascota eliminada",null);} private <T> ResponseEntity<ApiResponse<T>> ok(String m,T d){return ResponseEntity.ok(resp(HttpStatus.OK,m,d));} private <T> ApiResponse<T> resp(HttpStatus s,String m,T d){return ApiResponse.<T>builder().timestamp(LocalDateTime.now()).status(s.value()).mensaje(m).data(d).build();}}
