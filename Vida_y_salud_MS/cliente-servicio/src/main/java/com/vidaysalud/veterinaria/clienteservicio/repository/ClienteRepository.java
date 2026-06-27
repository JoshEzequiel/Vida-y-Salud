package com.vidaysalud.veterinaria.clienteservicio.repository;
import com.vidaysalud.veterinaria.clienteservicio.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ClienteRepository extends JpaRepository<Cliente,Integer>{ Optional<Cliente> findByRut(String rut); Optional<Cliente> findByEmail(String email); }
