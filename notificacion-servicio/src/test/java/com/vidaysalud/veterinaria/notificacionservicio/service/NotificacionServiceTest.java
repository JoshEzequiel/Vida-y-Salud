package com.vidaysalud.veterinaria.notificacionservicio.service;

import com.vidaysalud.veterinaria.notificacionservicio.client.ClienteClient;
import com.vidaysalud.veterinaria.notificacionservicio.dto.NotificacionRequestDTO;
import com.vidaysalud.veterinaria.notificacionservicio.dto.NotificacionResponseDTO;
import com.vidaysalud.veterinaria.notificacionservicio.exception.RecursoNoEncontradoException;
import com.vidaysalud.veterinaria.notificacionservicio.exception.ReglaNegocioException;
import com.vidaysalud.veterinaria.notificacionservicio.model.EstadoNotificacion;
import com.vidaysalud.veterinaria.notificacionservicio.model.Notificacion;
import com.vidaysalud.veterinaria.notificacionservicio.model.TipoNotificacion;
import com.vidaysalud.veterinaria.notificacionservicio.repository.NotificacionRepository;
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
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository repository;

    @Mock
    private ClienteClient clienteClient;

    @InjectMocks
    private NotificacionService service;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void listarNotificaciones_ok() {
        Notificacion notificacion = notificacionBase();

        when(repository.findAll())
                .thenReturn(List.of(notificacion));

        List<NotificacionResponseDTO> resultado = service.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdNotificacion());
        assertEquals(1, resultado.get(0).getIdCliente());
        assertEquals(EstadoNotificacion.PENDIENTE, resultado.get(0).getEstado());

        verify(repository).findAll();
    }

    @Test
    void buscarNotificacion_ok() {
        Notificacion notificacion = notificacionBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(notificacion));

        NotificacionResponseDTO resultado = service.buscar(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdNotificacion());
        assertEquals(1, resultado.getIdCliente());
        assertEquals("Recordatorio de cita", resultado.getAsunto());

        verify(repository).findById(1);
    }

    @Test
    void buscarNotificacion_noExiste_lanzaExcepcion() {
        when(repository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.buscar(999)
        );

        verify(repository).findById(999);
    }

    @Test
    void listarNotificacionesPorCliente_ok() {
        Notificacion notificacion = notificacionBase();

        when(repository.findByIdClienteOrderByFechaCreacionDesc(1))
                .thenReturn(List.of(notificacion));

        List<NotificacionResponseDTO> resultado = service.cliente(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdCliente());

        verify(repository).findByIdClienteOrderByFechaCreacionDesc(1);
    }

    @Test
    void crearNotificacion_conDataFaker_debeGuardarCorrectamente() {
        NotificacionRequestDTO request = requestBase();

        doNothing()
                .when(clienteClient)
                .validar(request.getIdCliente());

        when(repository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> {
                    Notificacion notificacion = invocation.getArgument(0);
                    notificacion.setIdNotificacion(1);
                    notificacion.setFechaCreacion(LocalDateTime.now());
                    return notificacion;
                });

        NotificacionResponseDTO resultado = service.crear(request);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdNotificacion());
        assertEquals(request.getIdCliente(), resultado.getIdCliente());
        assertEquals(request.getTipo(), resultado.getTipo());
        assertEquals(request.getDestinatario().trim(), resultado.getDestinatario());
        assertEquals(request.getAsunto().trim(), resultado.getAsunto());
        assertEquals(request.getMensaje().trim(), resultado.getMensaje());
        assertEquals(EstadoNotificacion.PENDIENTE, resultado.getEstado());

        verify(clienteClient).validar(request.getIdCliente());
        verify(repository).save(any(Notificacion.class));
    }

    @Test
    void crearNotificacion_clienteNoExiste_lanzaExcepcion() {
        NotificacionRequestDTO request = requestBase();

        doThrow(new ReglaNegocioException("Cliente no existe con id: " + request.getIdCliente()))
                .when(clienteClient)
                .validar(request.getIdCliente());

        assertThrows(
                ReglaNegocioException.class,
                () -> service.crear(request)
        );

        verify(clienteClient).validar(request.getIdCliente());
        verify(repository, never()).save(any(Notificacion.class));
    }

    @Test
    void enviarNotificacion_ok() {
        Notificacion notificacion = notificacionBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(notificacion));

        when(repository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificacionResponseDTO resultado = service.enviar(1);

        assertNotNull(resultado);
        assertEquals(EstadoNotificacion.ENVIADA, resultado.getEstado());
        assertNotNull(resultado.getFechaEnvio());

        verify(repository).findById(1);
        verify(repository).save(notificacion);
    }

    @Test
    void enviarNotificacion_yaEnviada_lanzaExcepcion() {
        Notificacion notificacion = notificacionBase();
        notificacion.setEstado(EstadoNotificacion.ENVIADA);
        notificacion.setFechaEnvio(LocalDateTime.now());

        when(repository.findById(1))
                .thenReturn(Optional.of(notificacion));

        assertThrows(
                ReglaNegocioException.class,
                () -> service.enviar(1)
        );

        verify(repository).findById(1);
        verify(repository, never()).save(any(Notificacion.class));
    }

    @Test
    void eliminarNotificacion_ok() {
        Notificacion notificacion = notificacionBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(notificacion));

        service.eliminar(1);

        verify(repository).findById(1);
        verify(repository).delete(notificacion);
    }

    @Test
    void eliminarNotificacion_noExiste_lanzaExcepcion() {
        when(repository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.eliminar(999)
        );

        verify(repository).findById(999);
        verify(repository, never()).delete(any(Notificacion.class));
    }

    private NotificacionRequestDTO requestBase() {
        NotificacionRequestDTO request = new NotificacionRequestDTO();
        request.setIdCliente(1);
        request.setTipo(TipoNotificacion.EMAIL);
        request.setDestinatario(faker.internet().emailAddress());
        request.setAsunto(faker.options().option(
                "Recordatorio de cita",
                "Confirmacion de atencion veterinaria",
                "Aviso de control pendiente",
                "Notificacion de consulta",
                "Recordatorio de tratamiento"
        ));
        request.setMensaje(faker.options().option(
                "Tiene una cita veterinaria agendada.",
                "Recuerde asistir al control de su mascota.",
                "Su mascota tiene una atencion pendiente.",
                "Se informa una notificacion importante de la veterinaria.",
                "Favor revisar los detalles de su proxima atencion."
        ));
        return request;
    }

    private Notificacion notificacionBase() {
        return Notificacion.builder()
                .idNotificacion(1)
                .idCliente(1)
                .tipo(TipoNotificacion.EMAIL)
                .destinatario("cliente@email.com")
                .asunto("Recordatorio de cita")
                .mensaje("Tiene una cita veterinaria agendada.")
                .estado(EstadoNotificacion.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .fechaEnvio(null)
                .build();
    }
}