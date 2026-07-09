package com.vidaysalud.veterinaria.clienteservicio.service;

import com.vidaysalud.veterinaria.clienteservicio.dto.ClienteRequestDTO;
import com.vidaysalud.veterinaria.clienteservicio.dto.ClienteResponseDTO;
import com.vidaysalud.veterinaria.clienteservicio.exception.DatoDuplicadoException;
import com.vidaysalud.veterinaria.clienteservicio.exception.RecursoNoEncontradoException;
import com.vidaysalud.veterinaria.clienteservicio.model.Cliente;
import com.vidaysalud.veterinaria.clienteservicio.repository.ClienteRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteService service;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void crearCliente_conDataFaker_debeGuardarCorrectamente() {
        // Given
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setRut("12345678-9");
        request.setNombre(faker.name().fullName());
        request.setTelefono("+56912345678");
        request.setEmail(faker.internet().emailAddress());
        request.setDireccion(faker.address().fullAddress());

        when(repository.findByRut(request.getRut()))
                .thenReturn(Optional.empty());

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(repository.save(any(Cliente.class)))
                .thenAnswer(invocation -> {
                    Cliente cliente = invocation.getArgument(0);
                    cliente.setIdCliente(1);
                    cliente.setFechaRegistro(LocalDateTime.now());
                    return cliente;
                });

        // When
        ClienteResponseDTO response = service.crear(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getIdCliente());
        assertEquals(request.getRut(), response.getRut());
        assertEquals(request.getNombre().trim(), response.getNombre());
        assertEquals(request.getEmail(), response.getEmail());

        verify(repository).findByRut(request.getRut());
        verify(repository).findByEmail(request.getEmail());
        verify(repository).save(any(Cliente.class));
    }

    @Test
    void crearCliente_rutDuplicado_lanzaExcepcion() {
        // Given
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setRut("12345678-9");
        request.setNombre("Cliente Duplicado");
        request.setTelefono("+56912345678");
        request.setEmail("duplicado@email.com");
        request.setDireccion("Puerto Montt");

        Cliente clienteExistente = Cliente.builder()
                .idCliente(1)
                .rut("12345678-9")
                .nombre("Cliente Existente")
                .email("existente@email.com")
                .build();

        when(repository.findByRut(request.getRut()))
                .thenReturn(Optional.of(clienteExistente));

        // When / Then
        assertThrows(
                DatoDuplicadoException.class,
                () -> service.crear(request)
        );

        verify(repository).findByRut(request.getRut());
        verify(repository, never()).save(any(Cliente.class));
    }

    @Test
    void buscarPorId_clienteNoExiste_lanzaExcepcion() {
        // Given
        Integer idInexistente = 999;

        when(repository.findById(idInexistente))
                .thenReturn(Optional.empty());

        // When / Then
        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.buscarPorId(idInexistente)
        );

        verify(repository).findById(idInexistente);
    }
}

