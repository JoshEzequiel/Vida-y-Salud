package com.vidaysalud.veterinaria.clienteservicio.dto;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClienteResponseDTO { private Integer idCliente; private String rut; private String nombre; private String telefono; private String email; private String direccion; private LocalDateTime fechaRegistro; }
