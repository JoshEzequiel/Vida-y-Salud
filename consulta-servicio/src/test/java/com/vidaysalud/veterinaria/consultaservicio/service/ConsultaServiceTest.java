package com.vidaysalud.veterinaria.consultaservicio.service;

import com.vidaysalud.veterinaria.consultaservicio.client.CitaClient;
import com.vidaysalud.veterinaria.consultaservicio.client.MascotaClient;
import com.vidaysalud.veterinaria.consultaservicio.client.VeterinarioClient;
import com.vidaysalud.veterinaria.consultaservicio.dto.ConsultaRequestDTO;
import com.vidaysalud.veterinaria.consultaservicio.dto.ConsultaResponseDTO;
import com.vidaysalud.veterinaria.consultaservicio.exception.DatoDuplicadoException;
import com.vidaysalud.veterinaria.consultaservicio.exception.RecursoNoEncontradoException;
import com.vidaysalud.veterinaria.consultaservicio.exception.ReglaNegocioException;
import com.vidaysalud.veterinaria.consultaservicio.model.Consulta;
import com.vidaysalud.veterinaria.consultaservicio.repository.ConsultaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository repository;

    @Mock
    private MascotaClient mascotaClient;

    @Mock
    private VeterinarioClient veterinarioClient;

    @Mock
    private CitaClient citaClient;

    @InjectMocks
    private ConsultaService service;

    @Test
    void listarConsultas_ok() {
        Consulta consulta = consultaBase();

        when(repository.findAll())
                .thenReturn(List.of(consulta));

        List<ConsultaResponseDTO> resultado = service.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdConsulta());
        assertEquals(1, resultado.get(0).getIdMascota());
        assertEquals("Control general", resultado.get(0).getMotivo());

        verify(repository).findAll();
    }

    @Test
    void buscarConsulta_ok() {
        Consulta consulta = consultaBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(consulta));

        ConsultaResponseDTO resultado = service.buscar(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdConsulta());
        assertEquals(1, resultado.getIdMascota());
        assertEquals(1, resultado.getIdVeterinario());
        assertEquals(1, resultado.getIdCita());

        verify(repository).findById(1);
    }

    @Test
    void buscarConsulta_noExiste_lanzaExcepcion() {
        when(repository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.buscar(999)
        );

        verify(repository).findById(999);
    }

    @Test
    void listarConsultasPorMascota_ok() {
        Consulta consulta = consultaBase();

        when(repository.findByIdMascota(1))
                .thenReturn(List.of(consulta));

        List<ConsultaResponseDTO> resultado = service.mascota(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdMascota());

        verify(repository).findByIdMascota(1);
    }

    @Test
    void crearConsulta_ok() {
        ConsultaRequestDTO request = requestBase();

        when(repository.findByIdCita(request.getIdCita()))
                .thenReturn(Optional.empty());

        when(repository.save(any(Consulta.class)))
                .thenAnswer(invocation -> {
                    Consulta consulta = invocation.getArgument(0);
                    consulta.setIdConsulta(1);
                    return consulta;
                });

        ConsultaResponseDTO resultado = service.crear(request);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdConsulta());
        assertEquals(request.getIdMascota(), resultado.getIdMascota());
        assertEquals(request.getIdVeterinario(), resultado.getIdVeterinario());
        assertEquals(request.getIdCita(), resultado.getIdCita());
        assertEquals(request.getMotivo(), resultado.getMotivo());
        assertEquals(request.getDiagnostico(), resultado.getDiagnostico());
        assertEquals(request.getCostoConsulta(), resultado.getCostoConsulta());

        verify(mascotaClient).validar(request.getIdMascota());
        verify(veterinarioClient).validar(request.getIdVeterinario());
        verify(citaClient).validar(request.getIdCita());
        verify(repository).findByIdCita(request.getIdCita());
        verify(repository).save(any(Consulta.class));
    }

    @Test
    void crearConsulta_citaDuplicada_lanzaExcepcion() {
        ConsultaRequestDTO request = requestBase();
        Consulta consultaExistente = consultaBase();

        when(repository.findByIdCita(request.getIdCita()))
                .thenReturn(Optional.of(consultaExistente));

        assertThrows(
                DatoDuplicadoException.class,
                () -> service.crear(request)
        );

        verify(mascotaClient).validar(request.getIdMascota());
        verify(veterinarioClient).validar(request.getIdVeterinario());
        verify(citaClient).validar(request.getIdCita());
        verify(repository).findByIdCita(request.getIdCita());
        verify(repository, never()).save(any(Consulta.class));
    }

    @Test
    void crearConsulta_mascotaNoExiste_lanzaExcepcion() {
        ConsultaRequestDTO request = requestBase();

        doThrow(new ReglaNegocioException("Mascota no existe con id: " + request.getIdMascota()))
                .when(mascotaClient)
                .validar(request.getIdMascota());

        assertThrows(
                ReglaNegocioException.class,
                () -> service.crear(request)
        );

        verify(mascotaClient).validar(request.getIdMascota());
        verify(veterinarioClient, never()).validar(any());
        verify(citaClient, never()).validar(any());
        verify(repository, never()).save(any(Consulta.class));
    }

    @Test
    void actualizarConsulta_ok() {
        Consulta existente = consultaBase();

        ConsultaRequestDTO request = requestBase();
        request.setMotivo("Control actualizado");
        request.setDiagnostico("Paciente estable");
        request.setCostoConsulta(new BigDecimal("45000.00"));

        when(repository.findByIdCita(request.getIdCita()))
                .thenReturn(Optional.empty());

        when(repository.findById(1))
                .thenReturn(Optional.of(existente));

        when(repository.save(any(Consulta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaResponseDTO resultado = service.actualizar(1, request);

        assertNotNull(resultado);
        assertEquals("Control actualizado", resultado.getMotivo());
        assertEquals("Paciente estable", resultado.getDiagnostico());
        assertEquals(new BigDecimal("45000.00"), resultado.getCostoConsulta());

        verify(mascotaClient).validar(request.getIdMascota());
        verify(veterinarioClient).validar(request.getIdVeterinario());
        verify(citaClient).validar(request.getIdCita());
        verify(repository).findByIdCita(request.getIdCita());
        verify(repository).findById(1);
        verify(repository).save(any(Consulta.class));
    }

    @Test
    void eliminarConsulta_ok() {
        Consulta consulta = consultaBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(consulta));

        service.eliminar(1);

        verify(repository).findById(1);
        verify(repository).delete(consulta);
    }

    private ConsultaRequestDTO requestBase() {
        ConsultaRequestDTO request = new ConsultaRequestDTO();
        request.setIdMascota(1);
        request.setIdVeterinario(1);
        request.setIdCita(1);
        request.setFechaConsulta(LocalDateTime.now());
        request.setMotivo("Control general");
        request.setDiagnostico("Paciente en buen estado");
        request.setObservaciones("Sin observaciones");
        request.setPesoActual(new BigDecimal("8.50"));
        request.setTemperatura(new BigDecimal("38.5"));
        request.setCostoConsulta(new BigDecimal("35000.00"));
        return request;
    }

    private Consulta consultaBase() {
        return Consulta.builder()
                .idConsulta(1)
                .idMascota(1)
                .idVeterinario(1)
                .idCita(1)
                .fechaConsulta(LocalDateTime.now())
                .motivo("Control general")
                .diagnostico("Paciente en buen estado")
                .observaciones("Sin observaciones")
                .pesoActual(new BigDecimal("8.50"))
                .temperatura(new BigDecimal("38.5"))
                .costoConsulta(new BigDecimal("35000.00"))
                .build();
    }
}