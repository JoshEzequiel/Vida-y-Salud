package com.vidaysalud.veterinaria.clienteservicio.controller;
import com.vidaysalud.veterinaria.clienteservicio.dto.*; import com.vidaysalud.veterinaria.clienteservicio.service.ClienteService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.time.LocalDateTime; import java.util.List;
@RestController @RequestMapping("/api/v1/clientes") @RequiredArgsConstructor
public class ClienteController {
 private final ClienteService service;
 @GetMapping public ResponseEntity<ApiResponse<List<ClienteResponseDTO>>> listar(){return ok("Clientes obtenidos",service.listar());}
 @GetMapping("/{id}") public ResponseEntity<ApiResponse<ClienteResponseDTO>> id(@PathVariable Integer id){return ok("Cliente encontrado",service.buscarPorId(id));}
 @GetMapping("/rut/{rut}") public ResponseEntity<ApiResponse<ClienteResponseDTO>> rut(@PathVariable String rut){return ok("Cliente encontrado",service.buscarPorRut(rut));}
 @PostMapping public ResponseEntity<ApiResponse<ClienteResponseDTO>> crear(@Valid @RequestBody ClienteRequestDTO d){return ResponseEntity.status(HttpStatus.CREATED).body(resp(HttpStatus.CREATED,"Cliente creado",service.crear(d)));}
 @PutMapping("/{id}") public ResponseEntity<ApiResponse<ClienteResponseDTO>> actualizar(@PathVariable Integer id,@Valid @RequestBody ClienteRequestDTO d){return ok("Cliente actualizado",service.actualizar(id,d));}
 @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id){service.eliminar(id);return ok("Cliente eliminado",null);}
 private <T> ResponseEntity<ApiResponse<T>> ok(String m,T d){return ResponseEntity.ok(resp(HttpStatus.OK,m,d));}
 private <T> ApiResponse<T> resp(HttpStatus s,String m,T d){return ApiResponse.<T>builder().timestamp(LocalDateTime.now()).status(s.value()).mensaje(m).data(d).build();}
}
