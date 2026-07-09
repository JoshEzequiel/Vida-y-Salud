package com.vidaysalud.veterinaria.citaservicio.service;

import com.vidaysalud.veterinaria.citaservicio.client.MascotaClient;
import com.vidaysalud.veterinaria.citaservicio.client.VeterinarioClient;
import com.vidaysalud.veterinaria.citaservicio.dto.CitaRequestDTO;
import com.vidaysalud.veterinaria.citaservicio.dto.CitaResponseDTO;
import com.vidaysalud.veterinaria.citaservicio.exception.DatoDuplicadoException;
import com.vidaysalud.veterinaria.citaservicio.exception.RecursoNoEncontradoException;
import com.vidaysalud.veterinaria.citaservicio.exception.ReglaNegocioException;
import com.vidaysalud.veterinaria.citaservicio.model.Cita;
import com.vidaysalud.veterinaria.citaservicio.model.EstadoCita;
import com.vidaysalud.veterinaria.citaservicio.repository.CitaRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository repository;

    @Mock
    private MascotaClient mascotaClient;

    @Mock
    private VeterinarioClient veterinarioClient;

    @InjectMocks
    private CitaService service;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void listarCitas_ok() {
        // Given
        Cita cita = citaBase();

        when(repository.findAll())
                .thenReturn(List.of(cita));

        // When
        List<CitaResponseDTO> resultado = service.listar();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(cita.getIdCita(), resultado.get(0).getIdCita());
        assertEquals(cita.getEstado(), resultado.get(0).getEstado());

        verify(repository).findAll();
    }

    @Test
    void buscarCita_ok() {
        // Given
        Cita cita = citaBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(cita));

        // When
        CitaResponseDTO resultado = service.buscar(1);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.getIdCita());
        assertEquals(1, resultado.getIdMascota());
        assertEquals(1, resultado.getIdVeterinario());

        verify(repository).findById(1);
    }

    @Test
    void buscarCita_noExiste_lanzaExcepcion() {
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
    void listarCitasPorMascota_ok() {
        // Given
        Cita cita = citaBase();

        when(repository.findByIdMascota(1))
                .thenReturn(List.of(cita));

        // When
        List<CitaResponseDTO> resultado = service.porMascota(1);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdMascota());

        verify(repository).findByIdMascota(1);
    }

    @Test
    void crearCita_conDataFaker_debeGuardarCorrectamente() {
        // Given
        CitaRequestDTO request = requestBase();

        doNothing()
                .when(mascotaClient)
                .validar(request.getIdMascota());

        doNothing()
                .when(veterinarioClient)
                .validar(request.getIdVeterinario());

        when(repository.existsByIdVeterinarioAndFechaHoraAndEstadoNot(
                request.getIdVeterinario(),
                request.getFechaHora(),
                EstadoCita.Cancelada
        )).thenReturn(false);

        when(repository.save(any(Cita.class)))
                .thenAnswer(invocation -> {
                    Cita cita = invocation.getArgument(0);
                    cita.setIdCita(1);
                    cita.setFechaCreacion(LocalDateTime.now());
                    return cita;
                });

        // When
        CitaResponseDTO resultado = service.crear(request);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.getIdCita());
        assertEquals(request.getIdMascota(), resultado.getIdMascota());
        assertEquals(request.getIdVeterinario(), resultado.getIdVeterinario());
        assertEquals(EstadoCita.Pendiente, resultado.getEstado());

        verify(mascotaClient).validar(request.getIdMascota());
        verify(veterinarioClient).validar(request.getIdVeterinario());
        verify(repository).existsByIdVeterinarioAndFechaHoraAndEstadoNot(
                request.getIdVeterinario(),
                request.getFechaHora(),
                EstadoCita.Cancelada
        );
        verify(repository).save(any(Cita.class));
    }

    @Test
    void crearCita_horarioDuplicado_lanzaExcepcion() {
        // Given
        CitaRequestDTO request = requestBase();

        doNothing()
                .when(mascotaClient)
                .validar(request.getIdMascota());

        doNothing()
                .when(veterinarioClient)
                .validar(request.getIdVeterinario());

        when(repository.existsByIdVeterinarioAndFechaHoraAndEstadoNot(
                request.getIdVeterinario(),
                request.getFechaHora(),
                EstadoCita.Cancelada
        )).thenReturn(true);

        // When / Then
        assertThrows(
                DatoDuplicadoException.class,
                () -> service.crear(request)
        );

        verify(mascotaClient).validar(request.getIdMascota());
        verify(veterinarioClient).validar(request.getIdVeterinario());
        verify(repository, never()).save(any(Cita.class));
    }

    @Test
    void crearCita_fechaPasada_lanzaExcepcion() {
        // Given
        CitaRequestDTO request = requestBase();
        request.setFechaHora(LocalDateTime.now().minusDays(1));

        doNothing()
                .when(mascotaClient)
                .validar(request.getIdMascota());

        doNothing()
                .when(veterinarioClient)
                .validar(request.getIdVeterinario());

        // When / Then
        assertThrows(
                ReglaNegocioException.class,
                () -> service.crear(request)
        );

        verify(mascotaClient).validar(request.getIdMascota());
        verify(veterinarioClient).validar(request.getIdVeterinario());
        verify(repository, never()).save(any(Cita.class));
    }

    @Test
    void crearCita_mascotaNoExiste_lanzaExcepcion() {
        // Given
        CitaRequestDTO request = requestBase();

        doThrow(new ReglaNegocioException("Mascota no existe con id: " + request.getIdMascota()))
                .when(mascotaClient)
                .validar(request.getIdMascota());

        // When / Then
        assertThrows(
                ReglaNegocioException.class,
                () -> service.crear(request)
        );

        verify(mascotaClient).validar(request.getIdMascota());
        verify(veterinarioClient, never()).validar(any());
        verify(repository, never()).save(any(Cita.class));
    }

    @Test
    void actualizarCita_ok() {
        // Given
        Cita existente = citaBase();
        CitaRequestDTO request = requestBase();
        request.setFechaHora(LocalDateTime.now().plusDays(3));
        request.setMotivo("Control actualizado");

        when(repository.findById(1))
                .thenReturn(Optional.of(existente));

        doNothing()
                .when(mascotaClient)
                .validar(request.getIdMascota());

        doNothing()
                .when(veterinarioClient)
                .validar(request.getIdVeterinario());

        when(repository.existsByIdVeterinarioAndFechaHoraAndEstadoNot(
                request.getIdVeterinario(),
                request.getFechaHora(),
                EstadoCita.Cancelada
        )).thenReturn(false);

        when(repository.save(any(Cita.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CitaResponseDTO resultado = service.actualizar(1, request);

        // Then
        assertNotNull(resultado);
        assertEquals("Control actualizado", resultado.getMotivo());
        assertEquals(request.getFechaHora(), resultado.getFechaHora());

        verify(repository).findById(1);
        verify(mascotaClient).validar(request.getIdMascota());
        verify(veterinarioClient).validar(request.getIdVeterinario());
        verify(repository).save(any(Cita.class));
    }

    @Test
    void actualizarEstado_ok() {
        // Given
        Cita cita = citaBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(cita));

        when(repository.save(any(Cita.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CitaResponseDTO resultado = service.estado(1, EstadoCita.Confirmada);

        // Then
        assertNotNull(resultado);
        assertEquals(EstadoCita.Confirmada, resultado.getEstado());

        verify(repository).findById(1);
        verify(repository).save(cita);
    }

    @Test
    void actualizarEstado_atendidaACancelada_lanzaExcepcion() {
        // Given
        Cita cita = citaBase();
        cita.setEstado(EstadoCita.Atendida);

        when(repository.findById(1))
                .thenReturn(Optional.of(cita));

        // When / Then
        assertThrows(
                ReglaNegocioException.class,
                () -> service.estado(1, EstadoCita.Cancelada)
        );

        verify(repository).findById(1);
        verify(repository, never()).save(any(Cita.class));
    }

    @Test
    void eliminarCita_ok() {
        // Given
        Cita cita = citaBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(cita));

        // When
        service.eliminar(1);

        // Then
        verify(repository).findById(1);
        verify(repository).delete(cita);
    }

    private CitaRequestDTO requestBase() {
        CitaRequestDTO request = new CitaRequestDTO();
        request.setIdMascota(1);
        request.setIdVeterinario(1);
        request.setFechaHora(LocalDateTime.now().plusDays(2));
        request.setEstado(null);
        request.setMotivo(faker.options().option(
                "Control general",
                "Vacunacion",
                "Revision de rutina",
                "Consulta por malestar",
                "Control post tratamiento"
        ));
        request.setObservaciones(faker.lorem().sentence());
        return request;
    }

    private Cita citaBase() {
        Cita cita = new Cita();
        cita.setIdCita(1);
        cita.setIdMascota(1);
        cita.setIdVeterinario(1);
        cita.setFechaHora(LocalDateTime.now().plusDays(2));
        cita.setEstado(EstadoCita.Pendiente);
        cita.setMotivo("Control general");
        cita.setObservaciones("Sin observaciones");
        cita.setFechaCreacion(LocalDateTime.now());
        return cita;
    }
}
