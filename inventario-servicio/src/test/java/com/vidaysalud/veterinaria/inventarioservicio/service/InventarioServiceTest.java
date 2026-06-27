package com.vidaysalud.veterinaria.inventarioservicio.service;

import com.vidaysalud.veterinaria.inventarioservicio.client.MedicamentoClient;
import com.vidaysalud.veterinaria.inventarioservicio.dto.MovimientoRequestDTO;
import com.vidaysalud.veterinaria.inventarioservicio.dto.MovimientoResponseDTO;
import com.vidaysalud.veterinaria.inventarioservicio.exception.RecursoNoEncontradoException;
import com.vidaysalud.veterinaria.inventarioservicio.exception.ReglaNegocioException;
import com.vidaysalud.veterinaria.inventarioservicio.model.MovimientoInventario;
import com.vidaysalud.veterinaria.inventarioservicio.model.TipoMovimiento;
import com.vidaysalud.veterinaria.inventarioservicio.repository.MovimientoInventarioRepository;
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
class InventarioServiceTest {

    @Mock
    private MovimientoInventarioRepository repository;

    @Mock
    private MedicamentoClient medicamentoClient;

    @InjectMocks
    private InventarioService service;

    @Test
    void listarMovimientos_ok() {
        MovimientoInventario movimiento = movimientoBase();

        when(repository.findAll())
                .thenReturn(List.of(movimiento));

        List<MovimientoResponseDTO> resultado = service.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdMovimiento());
        assertEquals(1, resultado.get(0).getIdMedicamento());
        assertEquals(TipoMovimiento.ENTRADA, resultado.get(0).getTipoMovimiento());

        verify(repository).findAll();
    }

    @Test
    void buscarMovimiento_ok() {
        MovimientoInventario movimiento = movimientoBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(movimiento));

        MovimientoResponseDTO resultado = service.buscar(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdMovimiento());
        assertEquals(1, resultado.getIdMedicamento());
        assertEquals(10, resultado.getCantidad());

        verify(repository).findById(1);
    }

    @Test
    void buscarMovimiento_noExiste_lanzaExcepcion() {
        when(repository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.buscar(999)
        );

        verify(repository).findById(999);
    }

    @Test
    void listarPorMedicamento_ok() {
        MovimientoInventario movimiento = movimientoBase();

        when(repository.findByIdMedicamentoOrderByFechaMovimientoDesc(1))
                .thenReturn(List.of(movimiento));

        List<MovimientoResponseDTO> resultado = service.porMedicamento(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdMedicamento());

        verify(repository).findByIdMedicamentoOrderByFechaMovimientoDesc(1);
    }

    @Test
    void registrarEntrada_ok() {
        MovimientoRequestDTO request = requestBase();
        request.setTipoMovimiento(TipoMovimiento.ENTRADA);
        request.setCantidad(10);

        when(medicamentoClient.obtenerStock(request.getIdMedicamento()))
                .thenReturn(20);

        when(repository.save(any(MovimientoInventario.class)))
                .thenAnswer(invocation -> {
                    MovimientoInventario movimiento = invocation.getArgument(0);
                    movimiento.setIdMovimiento(1);
                    movimiento.setFechaMovimiento(LocalDateTime.now());
                    return movimiento;
                });

        MovimientoResponseDTO resultado = service.registrar(request);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdMovimiento());
        assertEquals(1, resultado.getIdMedicamento());
        assertEquals(TipoMovimiento.ENTRADA, resultado.getTipoMovimiento());
        assertEquals(20, resultado.getStockAnterior());
        assertEquals(30, resultado.getStockPosterior());

        verify(medicamentoClient).obtenerStock(request.getIdMedicamento());
        verify(medicamentoClient).actualizarStock(request.getIdMedicamento(), 30);
        verify(repository).save(any(MovimientoInventario.class));
    }

    @Test
    void registrarSalida_ok() {
        MovimientoRequestDTO request = requestBase();
        request.setTipoMovimiento(TipoMovimiento.SALIDA);
        request.setCantidad(5);

        when(medicamentoClient.obtenerStock(request.getIdMedicamento()))
                .thenReturn(20);

        when(repository.save(any(MovimientoInventario.class)))
                .thenAnswer(invocation -> {
                    MovimientoInventario movimiento = invocation.getArgument(0);
                    movimiento.setIdMovimiento(1);
                    movimiento.setFechaMovimiento(LocalDateTime.now());
                    return movimiento;
                });

        MovimientoResponseDTO resultado = service.registrar(request);

        assertNotNull(resultado);
        assertEquals(TipoMovimiento.SALIDA, resultado.getTipoMovimiento());
        assertEquals(20, resultado.getStockAnterior());
        assertEquals(15, resultado.getStockPosterior());

        verify(medicamentoClient).obtenerStock(request.getIdMedicamento());
        verify(medicamentoClient).actualizarStock(request.getIdMedicamento(), 15);
        verify(repository).save(any(MovimientoInventario.class));
    }

    @Test
    void registrarSalida_stockInsuficiente_lanzaExcepcion() {
        MovimientoRequestDTO request = requestBase();
        request.setTipoMovimiento(TipoMovimiento.SALIDA);
        request.setCantidad(30);

        when(medicamentoClient.obtenerStock(request.getIdMedicamento()))
                .thenReturn(10);

        assertThrows(
                ReglaNegocioException.class,
                () -> service.registrar(request)
        );

        verify(medicamentoClient).obtenerStock(request.getIdMedicamento());
        verify(medicamentoClient, never()).actualizarStock(any(), any());
        verify(repository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void registrarAjuste_ok() {
        MovimientoRequestDTO request = requestBase();
        request.setTipoMovimiento(TipoMovimiento.AJUSTE);
        request.setCantidad(50);

        when(medicamentoClient.obtenerStock(request.getIdMedicamento()))
                .thenReturn(20);

        when(repository.save(any(MovimientoInventario.class)))
                .thenAnswer(invocation -> {
                    MovimientoInventario movimiento = invocation.getArgument(0);
                    movimiento.setIdMovimiento(1);
                    movimiento.setFechaMovimiento(LocalDateTime.now());
                    return movimiento;
                });

        MovimientoResponseDTO resultado = service.registrar(request);

        assertNotNull(resultado);
        assertEquals(TipoMovimiento.AJUSTE, resultado.getTipoMovimiento());
        assertEquals(20, resultado.getStockAnterior());
        assertEquals(50, resultado.getStockPosterior());

        verify(medicamentoClient).obtenerStock(request.getIdMedicamento());
        verify(medicamentoClient).actualizarStock(request.getIdMedicamento(), 50);
        verify(repository).save(any(MovimientoInventario.class));
    }

    private MovimientoRequestDTO requestBase() {
        MovimientoRequestDTO request = new MovimientoRequestDTO();
        request.setIdMedicamento(1);
        request.setTipoMovimiento(TipoMovimiento.ENTRADA);
        request.setCantidad(10);
        request.setMotivo("Movimiento de prueba");
        return request;
    }

    private MovimientoInventario movimientoBase() {
        return MovimientoInventario.builder()
                .idMovimiento(1)
                .idMedicamento(1)
                .tipoMovimiento(TipoMovimiento.ENTRADA)
                .cantidad(10)
                .stockAnterior(20)
                .stockPosterior(30)
                .fechaMovimiento(LocalDateTime.now())
                .motivo("Movimiento de prueba")
                .build();
    }
}