package com.vidaysalud.veterinaria.mascotaservicio.dto;
import com.vidaysalud.veterinaria.mascotaservicio.model.SexoMascota; import jakarta.validation.constraints.*; import lombok.*; import java.math.BigDecimal; import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MascotaRequestDTO{
 @NotNull private Integer idCliente; @NotBlank @Size(max=100) private String nombre; @NotBlank @Size(max=50) private String especie; @Size(max=100) private String raza; @PastOrPresent private LocalDate fechaNacimiento; @NotNull private SexoMascota sexo; @DecimalMin(value="0.0",inclusive=true) private BigDecimal peso; private Boolean activa;
}
