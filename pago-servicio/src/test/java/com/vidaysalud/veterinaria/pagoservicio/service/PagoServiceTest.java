package com.vidaysalud.veterinaria.pagoservicio.service;

import com.vidaysalud.veterinaria.pagoservicio.client.ConsultaClient;
import com.vidaysalud.veterinaria.pagoservicio.dto.PagoRequestDTO;
import com.vidaysalud.veterinaria.pagoservicio.dto.PagoResponseDTO;
import com.vidaysalud.veterinaria.pagoservicio.exception.DatoDuplicadoException;
import com.vidaysalud.veterinaria.pagoservicio.exception.RecursoNoEncontradoException;
import com.vidaysalud.veterinaria.pagoservicio.exception.ReglaNegocioException;
import com.vidaysalud.veterinaria.pagoservicio.model.EstadoPago;
import com.vidaysalud.veterinaria.pagoservicio.model.MetodoPago;
import com.vidaysalud.veterinaria.pagoservicio.model.Pago;
import com.vidaysalud.veterinaria.pagoservicio.repository.PagoRepository;
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
class PagoServiceTest {

    @Mock
    private PagoRepository repository;

    @Mock
    private ConsultaClient consultaClient;

    @InjectMocks
    private PagoService service;

    @Test
    void listarPagos_ok() {
        Pago pago = pagoBase();

        when(repository.findAll())
                .thenReturn(List.of(pago));

        List<PagoResponseDTO> resultado = service.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdPago());
        assertEquals(1, resultado.get(0).getIdConsulta());
        assertEquals(EstadoPago.PENDIENTE, resultado.get(0).getEstado());

        verify(repository).findAll();
    }

    @Test
    void buscarPago_ok() {
        Pago pago = pagoBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(pago));

        PagoResponseDTO resultado = service.buscar(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdPago());
        assertEquals(1, resultado.getIdConsulta());
        assertEquals(new BigDecimal("35000.00"), resultado.getMonto());

        verify(repository).findById(1);
    }

    @Test
    void buscarPago_noExiste_lanzaExcepcion() {
        when(repository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.buscar(999)
        );

        verify(repository).findById(999);
    }

    @Test
    void crearPago_ok() {
        PagoRequestDTO request = requestBase();

        doNothing()
                .when(consultaClient)
                .validar(request.getIdConsulta());

        when(repository.findByIdConsulta(request.getIdConsulta()))
                .thenReturn(Optional.empty());

        when(repository.save(any(Pago.class)))
                .thenAnswer(invocation -> {
                    Pago pago = invocation.getArgument(0);
                    pago.setIdPago(1);
                    return pago;
                });

        PagoResponseDTO resultado = service.crear(request);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdPago());
        assertEquals(request.getIdConsulta(), resultado.getIdConsulta());
        assertEquals(request.getMonto(), resultado.getMonto());
        assertEquals(MetodoPago.EFECTIVO, resultado.getMetodoPago());
        assertEquals(EstadoPago.PENDIENTE, resultado.getEstado());

        verify(consultaClient).validar(request.getIdConsulta());
        verify(repository).findByIdConsulta(request.getIdConsulta());
        verify(repository).save(any(Pago.class));
    }

    @Test
    void crearPago_consultaYaTienePago_lanzaExcepcion() {
        PagoRequestDTO request = requestBase();
        Pago pagoExistente = pagoBase();

        doNothing()
                .when(consultaClient)
                .validar(request.getIdConsulta());

        when(repository.findByIdConsulta(request.getIdConsulta()))
                .thenReturn(Optional.of(pagoExistente));

        assertThrows(
                DatoDuplicadoException.class,
                () -> service.crear(request)
        );

        verify(consultaClient).validar(request.getIdConsulta());
        verify(repository).findByIdConsulta(request.getIdConsulta());
        verify(repository, never()).save(any(Pago.class));
    }

    @Test
    void crearPago_consultaNoExiste_lanzaExcepcion() {
        PagoRequestDTO request = requestBase();

        doThrow(new ReglaNegocioException("Consulta no existe con id: " + request.getIdConsulta()))
                .when(consultaClient)
                .validar(request.getIdConsulta());

        assertThrows(
                ReglaNegocioException.class,
                () -> service.crear(request)
        );

        verify(consultaClient).validar(request.getIdConsulta());
        verify(repository, never()).findByIdConsulta(any());
        verify(repository, never()).save(any(Pago.class));
    }

    @Test
    void actualizarPago_mismaConsulta_ok() {
        Pago pago = pagoBase();

        PagoRequestDTO request = requestBase();
        request.setMonto(new BigDecimal("45000.00"));
        request.setMetodoPago(MetodoPago.TARJETA);
        request.setReferencia("REF-123");

        when(repository.findById(1))
                .thenReturn(Optional.of(pago));

        when(repository.save(any(Pago.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PagoResponseDTO resultado = service.actualizar(1, request);

        assertNotNull(resultado);
        assertEquals(new BigDecimal("45000.00"), resultado.getMonto());
        assertEquals(MetodoPago.TARJETA, resultado.getMetodoPago());
        assertEquals("REF-123", resultado.getReferencia());

        verify(repository).findById(1);
        verify(consultaClient, never()).validar(any());
        verify(repository, never()).findByIdConsulta(any());
        verify(repository).save(any(Pago.class));
    }

    @Test
    void actualizarPago_cambiaConsulta_ok() {
        Pago pago = pagoBase();

        PagoRequestDTO request = requestBase();
        request.setIdConsulta(2);

        when(repository.findById(1))
                .thenReturn(Optional.of(pago));

        doNothing()
                .when(consultaClient)
                .validar(request.getIdConsulta());

        when(repository.findByIdConsulta(request.getIdConsulta()))
                .thenReturn(Optional.empty());

        when(repository.save(any(Pago.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PagoResponseDTO resultado = service.actualizar(1, request);

        assertNotNull(resultado);
        assertEquals(2, resultado.getIdConsulta());

        verify(repository).findById(1);
        verify(consultaClient).validar(request.getIdConsulta());
        verify(repository).findByIdConsulta(request.getIdConsulta());
        verify(repository).save(any(Pago.class));
    }

    @Test
    void actualizarEstado_pagado_ok() {
        Pago pago = pagoBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(pago));

        when(repository.save(any(Pago.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PagoResponseDTO resultado = service.estado(1, EstadoPago.PAGADO);

        assertNotNull(resultado);
        assertEquals(EstadoPago.PAGADO, resultado.getEstado());
        assertNotNull(resultado.getFechaPago());

        verify(repository).findById(1);
        verify(repository).save(pago);
    }

    @Test
    void actualizarEstado_anuladoAPagado_lanzaExcepcion() {
        Pago pago = pagoBase();
        pago.setEstado(EstadoPago.ANULADO);

        when(repository.findById(1))
                .thenReturn(Optional.of(pago));

        assertThrows(
                ReglaNegocioException.class,
                () -> service.estado(1, EstadoPago.PAGADO)
        );

        verify(repository).findById(1);
        verify(repository, never()).save(any(Pago.class));
    }

    @Test
    void eliminarPago_ok() {
        Pago pago = pagoBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(pago));

        service.eliminar(1);

        verify(repository).findById(1);
        verify(repository).delete(pago);
    }

    private PagoRequestDTO requestBase() {
        PagoRequestDTO request = new PagoRequestDTO();
        request.setIdConsulta(1);
        request.setMonto(new BigDecimal("35000.00"));
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setEstado(null);
        request.setReferencia("Pago de prueba");
        return request;
    }

    private Pago pagoBase() {
        return Pago.builder()
                .idPago(1)
                .idConsulta(1)
                .monto(new BigDecimal("35000.00"))
                .metodoPago(MetodoPago.EFECTIVO)
                .estado(EstadoPago.PENDIENTE)
                .fechaPago(null)
                .referencia("Pago de prueba")
                .build();
    }
}