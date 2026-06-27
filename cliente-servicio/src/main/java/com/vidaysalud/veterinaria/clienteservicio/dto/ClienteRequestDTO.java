package com.vidaysalud.veterinaria.clienteservicio.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ClienteRequestDTO {
 @NotBlank(message="El RUT es obligatorio") @Size(max=20) private String rut;
 @NotBlank(message="El nombre es obligatorio") @Size(max=100) private String nombre;
 @Pattern(regexp="^[0-9+\\- ]{8,20}$",message="El teléfono no tiene un formato válido") private String telefono;
 @Email(message="El correo no tiene un formato válido") @Size(max=100) private String email;
 @Size(max=200) private String direccion;
}
