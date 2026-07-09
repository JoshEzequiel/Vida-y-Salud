package com.vidaysalud.veterinaria.recetaservicio.service;

import com.vidaysalud.veterinaria.recetaservicio.client.ConsultaClient;
import com.vidaysalud.veterinaria.recetaservicio.client.MedicamentoClient;
import com.vidaysalud.veterinaria.recetaservicio.dto.DetalleRecetaRequestDTO;
import com.vidaysalud.veterinaria.recetaservicio.dto.RecetaRequestDTO;
import com.vidaysalud.veterinaria.recetaservicio.dto.RecetaResponseDTO;
import com.vidaysalud.veterinaria.recetaservicio.exception.DatoDuplicadoException;
import com.vidaysalud.veterinaria.recetaservicio.exception.RecursoNoEncontradoException;
import com.vidaysalud.veterinaria.recetaservicio.exception.ReglaNegocioException;
import com.vidaysalud.veterinaria.recetaservicio.model.DetalleReceta;
import com.vidaysalud.veterinaria.recetaservicio.model.Receta;
import com.vidaysalud.veterinaria.recetaservicio.repository.RecetaRepository;
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
class RecetaServiceTest {

    @Mock
    private RecetaRepository repository;

    @Mock
    private ConsultaClient consultaClient;

    @Mock
    private MedicamentoClient medicamentoClient;

    @InjectMocks
    private RecetaService service;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void listarRecetas_ok() {
        Receta receta = recetaBase();

        when(repository.findAll())
                .thenReturn(List.of(receta));

        when(repository.findConDetallesByIdReceta(1))
                .thenReturn(Optional.of(receta));

        List<RecetaResponseDTO> resultado = service.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdReceta());
        assertEquals(1, resultado.get(0).getIdConsulta());
        assertEquals(1, resultado.get(0).getDetalles().size());

        verify(repository).findAll();
        verify(repository).findConDetallesByIdReceta(1);
    }

    @Test
    void buscarReceta_ok() {
        Receta receta = recetaBase();

        when(repository.findConDetallesByIdReceta(1))
                .thenReturn(Optional.of(receta));

        RecetaResponseDTO resultado = service.buscar(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdReceta());
        assertEquals(1, resultado.getIdConsulta());
        assertEquals("Tomar medicamento con comida", resultado.getIndicacionesGenerales());
        assertEquals(1, resultado.getDetalles().size());

        verify(repository).findConDetallesByIdReceta(1);
    }

    @Test
    void buscarReceta_noExiste_lanzaExcepcion() {
        when(repository.findConDetallesByIdReceta(999))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.buscar(999)
        );

        verify(repository).findConDetallesByIdReceta(999);
    }

    @Test
    void crearReceta_conDataFaker_debeGuardarCorrectamente() {
        RecetaRequestDTO request = requestBase();

        doNothing()
                .when(consultaClient)
                .validar(request.getIdConsulta());

        doNothing()
                .when(medicamentoClient)
                .validar(1);

        when(repository.save(any(Receta.class)))
                .thenAnswer(invocation -> {
                    Receta receta = invocation.getArgument(0);
                    receta.setIdReceta(1);
                    receta.setFechaEmision(LocalDateTime.now());
                    return receta;
                });

        RecetaResponseDTO resultado = service.crear(request);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdReceta());
        assertEquals(request.getIdConsulta(), resultado.getIdConsulta());
        assertEquals(request.getIndicacionesGenerales(), resultado.getIndicacionesGenerales());
        assertEquals(1, resultado.getDetalles().size());
        assertEquals(1, resultado.getDetalles().get(0).getIdMedicamento());

        verify(consultaClient).validar(request.getIdConsulta());
        verify(medicamentoClient).validar(1);
        verify(repository).save(any(Receta.class));
    }

    @Test
    void crearReceta_consultaNoExiste_lanzaExcepcion() {
        RecetaRequestDTO request = requestBase();

        doThrow(new ReglaNegocioException("Consulta no existe con id: " + request.getIdConsulta()))
                .when(consultaClient)
                .validar(request.getIdConsulta());

        assertThrows(
                ReglaNegocioException.class,
                () -> service.crear(request)
        );

        verify(consultaClient).validar(request.getIdConsulta());
        verify(medicamentoClient, never()).validar(any());
        verify(repository, never()).save(any(Receta.class));
    }

    @Test
    void crearReceta_medicamentoRepetido_lanzaExcepcion() {
        RecetaRequestDTO request = requestConMedicamentoRepetido();

        doNothing()
                .when(consultaClient)
                .validar(request.getIdConsulta());

        doNothing()
                .when(medicamentoClient)
                .validar(1);

        assertThrows(
                DatoDuplicadoException.class,
                () -> service.crear(request)
        );

        verify(consultaClient).validar(request.getIdConsulta());
        verify(medicamentoClient, times(2)).validar(1);
        verify(repository, never()).save(any(Receta.class));
    }

    @Test
    void actualizarReceta_ok() {
        Receta existente = recetaBase();

        RecetaRequestDTO request = requestBase();
        request.setIndicacionesGenerales("Nuevas indicaciones generales");

        when(repository.findConDetallesByIdReceta(1))
                .thenReturn(Optional.of(existente));

        when(repository.save(any(Receta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecetaResponseDTO resultado = service.actualizar(1, request);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdReceta());
        assertEquals("Nuevas indicaciones generales", resultado.getIndicacionesGenerales());
        assertEquals(1, resultado.getDetalles().size());
        assertEquals(1, resultado.getDetalles().get(0).getIdMedicamento());

        verify(consultaClient).validar(request.getIdConsulta());
        verify(medicamentoClient).validar(1);
        verify(repository).findConDetallesByIdReceta(1);
        verify(repository).save(any(Receta.class));
    }

    @Test
    void actualizarReceta_noExiste_lanzaExcepcion() {
        RecetaRequestDTO request = requestBase();

        when(repository.findConDetallesByIdReceta(999))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.actualizar(999, request)
        );

        verify(consultaClient).validar(request.getIdConsulta());
        verify(medicamentoClient).validar(1);
        verify(repository).findConDetallesByIdReceta(999);
        verify(repository, never()).save(any(Receta.class));
    }

    @Test
    void eliminarReceta_ok() {
        Receta receta = recetaBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(receta));

        service.eliminar(1);

        verify(repository).findById(1);
        verify(repository).delete(receta);
    }

    @Test
    void eliminarReceta_noExiste_lanzaExcepcion() {
        when(repository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.eliminar(999)
        );

        verify(repository).findById(999);
        verify(repository, never()).delete(any(Receta.class));
    }

    private RecetaRequestDTO requestBase() {
        RecetaRequestDTO request = new RecetaRequestDTO();
        request.setIdConsulta(1);
        request.setIndicacionesGenerales(faker.options().option(
                "Tomar medicamento con comida",
                "Administrar tratamiento según indicación veterinaria",
                "Mantener observación durante el tratamiento",
                "Completar tratamiento indicado"
        ));
        request.setDetalles(List.of(detalleRequest(1)));
        return request;
    }

    private RecetaRequestDTO requestConMedicamentoRepetido() {
        RecetaRequestDTO request = new RecetaRequestDTO();
        request.setIdConsulta(1);
        request.setIndicacionesGenerales(faker.options().option(
                "Tratamiento con medicamentos repetidos",
                "Validación de medicamentos duplicados",
                "Prueba de regla de negocio en receta"
        ));
        request.setDetalles(List.of(
                detalleRequest(1),
                detalleRequest(1)
        ));
        return request;
    }

    private DetalleRecetaRequestDTO detalleRequest(Integer idMedicamento) {
        DetalleRecetaRequestDTO detalle = new DetalleRecetaRequestDTO();
        detalle.setIdMedicamento(idMedicamento);
        detalle.setDosis(faker.options().option(
                "1 comprimido",
                "2 ml",
                "5 gotas",
                "1 dosis"
        ));
        detalle.setFrecuencia(faker.options().option(
                "Cada 8 horas",
                "Cada 12 horas",
                "Una vez al dia",
                "Cada 24 horas"
        ));
        detalle.setDuracion(faker.options().option(
                "3 dias",
                "5 dias",
                "7 dias",
                "10 dias"
        ));
        detalle.setIndicaciones(faker.lorem().sentence());
        return detalle;
    }

    private Receta recetaBase() {
        Receta receta = Receta.builder()
                .idReceta(1)
                .idConsulta(1)
                .fechaEmision(LocalDateTime.now())
                .indicacionesGenerales("Tomar medicamento con comida")
                .build();

        DetalleReceta detalle = DetalleReceta.builder()
                .idDetalle(1)
                .idMedicamento(1)
                .dosis("1 comprimido")
                .frecuencia("Cada 12 horas")
                .duracion("5 días")
                .indicaciones("Administrar después de comer")
                .build();

        receta.agregarDetalle(detalle);

        return receta;
    }
}