package com.vidaysalud.veterinaria.mascotaservicio.service;

import com.vidaysalud.veterinaria.mascotaservicio.client.ClienteClient;
import com.vidaysalud.veterinaria.mascotaservicio.dto.MascotaRequestDTO;
import com.vidaysalud.veterinaria.mascotaservicio.dto.MascotaResponseDTO;
import com.vidaysalud.veterinaria.mascotaservicio.exception.RecursoNoEncontradoException;
import com.vidaysalud.veterinaria.mascotaservicio.exception.ReglaNegocioException;
import com.vidaysalud.veterinaria.mascotaservicio.model.Mascota;
import com.vidaysalud.veterinaria.mascotaservicio.repository.MascotaRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MascotaServiceTest {

    @Mock
    private MascotaRepository repository;

    @Mock
    private ClienteClient clienteClient;

    @InjectMocks
    private MascotaService service;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void listarMascotas_ok() {
        // Given
        Mascota mascota = mascotaBase();

        when(repository.findAll())
                .thenReturn(List.of(mascota));

        // When
        List<MascotaResponseDTO> resultado = service.listar();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(mascota.getIdMascota(), resultado.get(0).getIdMascota());
        assertEquals(mascota.getNombre(), resultado.get(0).getNombre());

        verify(repository).findAll();
    }

    @Test
    void buscarMascota_ok() {
        // Given
        Mascota mascota = mascotaBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(mascota));

        // When
        MascotaResponseDTO resultado = service.buscar(1);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.getIdMascota());
        assertEquals("Luna", resultado.getNombre());

        verify(repository).findById(1);
    }

    @Test
    void buscarMascota_noExiste_lanzaExcepcion() {
        // Given
        when(repository.findById(999))
                .thenReturn(Optional.empty());

        // When / Then
        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.buscar(999)
        );

        verify(repository).findById(999);
    }

    @Test
    void listarMascotasPorCliente_ok() {
        // Given
        Mascota mascota = mascotaBase();

        when(repository.findByIdCliente(1))
                .thenReturn(List.of(mascota));

        // When
        List<MascotaResponseDTO> resultado = service.porCliente(1);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdCliente());

        verify(repository).findByIdCliente(1);
    }

    @Test
    void crearMascota_ok() {
        // Given
        MascotaRequestDTO request = requestBase();

        doNothing()
                .when(clienteClient)
                .validar(request.getIdCliente());

        when(repository.save(any(Mascota.class)))
                .thenAnswer(invocation -> {
                    Mascota mascota = invocation.getArgument(0);
                    mascota.setIdMascota(1);
                    return mascota;
                });

        // When
        MascotaResponseDTO resultado = service.crear(request);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.getIdMascota());
        assertEquals(request.getIdCliente(), resultado.getIdCliente());
        assertEquals(request.getNombre().trim(), resultado.getNombre());
        assertEquals(request.getEspecie().trim(), resultado.getEspecie());
        assertTrue(resultado.getActiva());

        verify(clienteClient).validar(request.getIdCliente());
        verify(repository).save(any(Mascota.class));
    }

    @Test
    void crearMascota_clienteNoExiste_lanzaExcepcion() {
        // Given
        MascotaRequestDTO request = requestBase();

        doThrow(new ReglaNegocioException("Cliente no existe con id: " + request.getIdCliente()))
                .when(clienteClient)
                .validar(request.getIdCliente());

        // When / Then
        assertThrows(
                ReglaNegocioException.class,
                () -> service.crear(request)
        );

        verify(clienteClient).validar(request.getIdCliente());
        verify(repository, never()).save(any(Mascota.class));
    }

    @Test
    void actualizarMascota_ok() {
        // Given
        Mascota existente = mascotaBase();
        MascotaRequestDTO request = requestBase();
        request.setNombre("Michi Actualizado");
        request.setEspecie("Gato");

        when(repository.findById(1))
                .thenReturn(Optional.of(existente));

        doNothing()
                .when(clienteClient)
                .validar(request.getIdCliente());

        when(repository.save(any(Mascota.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MascotaResponseDTO resultado = service.actualizar(1, request);

        // Then
        assertNotNull(resultado);
        assertEquals("Michi Actualizado", resultado.getNombre());
        assertEquals("Gato", resultado.getEspecie());

        verify(repository).findById(1);
        verify(clienteClient).validar(request.getIdCliente());
        verify(repository).save(any(Mascota.class));
    }

    @Test
    void eliminarMascota_ok() {
        // Given
        Mascota mascota = mascotaBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(mascota));

        // When
        service.eliminar(1);

        // Then
        verify(repository).findById(1);
        verify(repository).delete(mascota);
    }

    private MascotaRequestDTO requestBase() {
        MascotaRequestDTO request = new MascotaRequestDTO();
        request.setIdCliente(1);
        request.setNombre(faker.cat().name());
        request.setEspecie("Gato");
        request.setRaza("Mestiza");
        request.setActiva(true);
        return request;
    }

    private Mascota mascotaBase() {
        Mascota mascota = new Mascota();
        mascota.setIdMascota(1);
        mascota.setIdCliente(1);
        mascota.setNombre("Luna");
        mascota.setEspecie("Gato");
        mascota.setRaza("Mestiza");
        mascota.setActiva(true);
        return mascota;
    }
}
