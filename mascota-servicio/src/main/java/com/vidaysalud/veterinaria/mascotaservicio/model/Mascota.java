package com.vidaysalud.veterinaria.mascotaservicio.model;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="mascota") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mascota{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_mascota") private Integer idMascota;
 @Column(name="id_cliente",nullable=false) private Integer idCliente;
 @Column(nullable=false,length=100) private String nombre;
 @Column(nullable=false,length=50) private String especie;
 @Column(length=100) private String raza;
 @Column(name="fecha_nacimiento") private LocalDate fechaNacimiento;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private SexoMascota sexo;
 @Column(precision=5,scale=2) private BigDecimal peso;
 @Column(nullable=false) private Boolean activa;
 @PrePersist void pre(){if(activa==null)activa=true;}
}
