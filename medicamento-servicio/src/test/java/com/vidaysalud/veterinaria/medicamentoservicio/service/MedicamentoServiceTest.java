package com.vidaysalud.veterinaria.medicamentoservicio.service;

import com.vidaysalud.veterinaria.medicamentoservicio.dto.MedicamentoRequestDTO;
import com.vidaysalud.veterinaria.medicamentoservicio.dto.MedicamentoResponseDTO;
import com.vidaysalud.veterinaria.medicamentoservicio.exception.DatoDuplicadoException;
import com.vidaysalud.veterinaria.medicamentoservicio.exception.RecursoNoEncontradoException;
import com.vidaysalud.veterinaria.medicamentoservicio.exception.ReglaNegocioException;
import com.vidaysalud.veterinaria.medicamentoservicio.model.Medicamento;
import com.vidaysalud.veterinaria.medicamentoservicio.repository.MedicamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicamentoServiceTest {

    @Mock
    private MedicamentoRepository repository;

    @InjectMocks
    private MedicamentoService service;

    @Test
    void listarMedicamentos_ok() {
        Medicamento medicamento = medicamentoBase();

        when(repository.findAll())
                .thenReturn(List.of(medicamento));

        List<MedicamentoResponseDTO> resultado = service.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdMedicamento());
        assertEquals("Amoxicilina", resultado.get(0).getNombre());
        assertEquals(10, resultado.get(0).getStock());

        verify(repository).findAll();
    }

    @Test
    void buscarMedicamento_ok() {
        Medicamento medicamento = medicamentoBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(medicamento));

        MedicamentoResponseDTO resultado = service.buscar(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdMedicamento());
        assertEquals("Amoxicilina", resultado.getNombre());
        assertEquals(new BigDecimal("12990.00"), resultado.getPrecioUnitario());

        verify(repository).findById(1);
    }

    @Test
    void buscarMedicamento_noExiste_lanzaExcepcion() {
        when(repository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.buscar(999)
        );

        verify(repository).findById(999);
    }

    @Test
    void crearMedicamento_ok() {
        MedicamentoRequestDTO request = requestBase();

        when(repository.findByNombreIgnoreCase(request.getNombre().trim()))
                .thenReturn(Optional.empty());

        when(repository.save(any(Medicamento.class)))
                .thenAnswer(invocation -> {
                    Medicamento medicamento = invocation.getArgument(0);
                    medicamento.setIdMedicamento(1);
                    return medicamento;
                });

        MedicamentoResponseDTO resultado = service.crear(request);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdMedicamento());
        assertEquals(request.getNombre().trim(), resultado.getNombre());
        assertEquals(request.getStock(), resultado.getStock());
        assertEquals(request.getPrecioUnitario(), resultado.getPrecioUnitario());
        assertTrue(resultado.getActivo());

        verify(repository).findByNombreIgnoreCase(request.getNombre().trim());
        verify(repository).save(any(Medicamento.class));
    }

    @Test
    void crearMedicamento_nombreDuplicado_lanzaExcepcion() {
        MedicamentoRequestDTO request = requestBase();
        Medicamento existente = medicamentoBase();

        when(repository.findByNombreIgnoreCase(request.getNombre().trim()))
                .thenReturn(Optional.of(existente));

        assertThrows(
                DatoDuplicadoException.class,
                () -> service.crear(request)
        );

        verify(repository).findByNombreIgnoreCase(request.getNombre().trim());
        verify(repository, never()).save(any(Medicamento.class));
    }

    @Test
    void actualizarMedicamento_ok() {
        Medicamento existente = medicamentoBase();

        MedicamentoRequestDTO request = requestBase();
        request.setNombre("Paracetamol");
        request.setDescripcion("Analgésico para control de dolor");
        request.setStock(20);
        request.setPrecioUnitario(new BigDecimal("4990.00"));

        when(repository.findByNombreIgnoreCase(request.getNombre().trim()))
                .thenReturn(Optional.empty());

        when(repository.findById(1))
                .thenReturn(Optional.of(existente));

        when(repository.save(any(Medicamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MedicamentoResponseDTO resultado = service.actualizar(1, request);

        assertNotNull(resultado);
        assertEquals("Paracetamol", resultado.getNombre());
        assertEquals("Analgésico para control de dolor", resultado.getDescripcion());
        assertEquals(20, resultado.getStock());
        assertEquals(new BigDecimal("4990.00"), resultado.getPrecioUnitario());

        verify(repository).findByNombreIgnoreCase(request.getNombre().trim());
        verify(repository).findById(1);
        verify(repository).save(any(Medicamento.class));
    }

    @Test
    void actualizarMedicamento_nombreDuplicado_lanzaExcepcion() {
        MedicamentoRequestDTO request = requestBase();
        request.setNombre("Medicamento Repetido");

        Medicamento otroMedicamento = Medicamento.builder()
                .idMedicamento(2)
                .nombre("Medicamento Repetido")
                .descripcion("Otro medicamento")
                .stock(5)
                .precioUnitario(new BigDecimal("3000.00"))
                .activo(true)
                .build();

        when(repository.findByNombreIgnoreCase(request.getNombre().trim()))
                .thenReturn(Optional.of(otroMedicamento));

        assertThrows(
                DatoDuplicadoException.class,
                () -> service.actualizar(1, request)
        );

        verify(repository).findByNombreIgnoreCase(request.getNombre().trim());
        verify(repository, never()).save(any(Medicamento.class));
    }

    @Test
    void actualizarStock_ok() {
        Medicamento medicamento = medicamentoBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(medicamento));

        when(repository.save(any(Medicamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MedicamentoResponseDTO resultado = service.actualizarStock(1, 50);

        assertNotNull(resultado);
        assertEquals(50, resultado.getStock());

        verify(repository).findById(1);
        verify(repository).save(medicamento);
    }

    @Test
    void actualizarStock_negativo_lanzaExcepcion() {
        assertThrows(
                ReglaNegocioException.class,
                () -> service.actualizarStock(1, -5)
        );

        verify(repository, never()).findById(any());
        verify(repository, never()).save(any(Medicamento.class));
    }

    @Test
    void eliminarMedicamento_ok() {
        Medicamento medicamento = medicamentoBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(medicamento));

        service.eliminar(1);

        verify(repository).findById(1);
        verify(repository).delete(medicamento);
    }

    private MedicamentoRequestDTO requestBase() {
        MedicamentoRequestDTO request = new MedicamentoRequestDTO();
        request.setNombre("Amoxicilina");
        request.setDescripcion("Antibiótico de uso veterinario");
        request.setStock(10);
        request.setPrecioUnitario(new BigDecimal("12990.00"));
        request.setActivo(true);
        return request;
    }

    private Medicamento medicamentoBase() {
        return Medicamento.builder()
                .idMedicamento(1)
                .nombre("Amoxicilina")
                .descripcion("Antibiótico de uso veterinario")
                .stock(10)
                .precioUnitario(new BigDecimal("12990.00"))
                .activo(true)
                .build();
    }
}