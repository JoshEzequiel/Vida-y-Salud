package com.vidaysalud.veterinaria.clienteservicio.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name="cliente", uniqueConstraints={@UniqueConstraint(name="uq_cliente_rut",columnNames="rut"),@UniqueConstraint(name="uq_cliente_email",columnNames="email")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_cliente") private Integer idCliente;
 @Column(nullable=false,length=20,unique=true) private String rut;
 @Column(nullable=false,length=100) private String nombre;
 @Column(length=20) private String telefono;
 @Column(length=100,unique=true) private String email;
 @Column(length=200) private String direccion;
 @Column(name="fecha_registro",nullable=false,updatable=false) private LocalDateTime fechaRegistro;
 @PrePersist void prePersist(){ if(fechaRegistro==null) fechaRegistro=LocalDateTime.now(); }
}
