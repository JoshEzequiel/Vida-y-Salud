package com.vidaysalud.veterinaria.veterinarioservicio.service;

import com.vidaysalud.veterinaria.veterinarioservicio.dto.VeterinarioRequestDTO;
import com.vidaysalud.veterinaria.veterinarioservicio.dto.VeterinarioResponseDTO;
import com.vidaysalud.veterinaria.veterinarioservicio.exception.DatoDuplicadoException;
import com.vidaysalud.veterinaria.veterinarioservicio.exception.RecursoNoEncontradoException;
import com.vidaysalud.veterinaria.veterinarioservicio.model.Veterinario;
import com.vidaysalud.veterinaria.veterinarioservicio.repository.VeterinarioRepository;
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
class VeterinarioServiceTest {

    @Mock
    private VeterinarioRepository repository;

    @InjectMocks
    private VeterinarioService service;

    @Test
    void listarVeterinarios_ok() {
        Veterinario veterinario = veterinarioBase();

        when(repository.findAll())
                .thenReturn(List.of(veterinario));

        List<VeterinarioResponseDTO> resultado = service.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getIdVeterinario());
        assertEquals("Juan Perez", resultado.get(0).getNombre());
        assertEquals("Cirugia", resultado.get(0).getEspecialidad());

        verify(repository).findAll();
    }

    @Test
    void buscarVeterinario_ok() {
        Veterinario veterinario = veterinarioBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(veterinario));

        VeterinarioResponseDTO resultado = service.buscar(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdVeterinario());
        assertEquals("Juan Perez", resultado.getNombre());
        assertEquals("juan.perez@veterinaria.cl", resultado.getEmail());

        verify(repository).findById(1);
    }

    @Test
    void buscarVeterinario_noExiste_lanzaExcepcion() {
        when(repository.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.buscar(999)
        );

        verify(repository).findById(999);
    }

    @Test
    void listarPorEspecialidad_ok() {
        Veterinario veterinario = veterinarioBase();

        when(repository.findByEspecialidadIgnoreCase("Cirugia"))
                .thenReturn(List.of(veterinario));

        List<VeterinarioResponseDTO> resultado = service.especialidad("Cirugia");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Cirugia", resultado.get(0).getEspecialidad());

        verify(repository).findByEspecialidadIgnoreCase("Cirugia");
    }

    @Test
    void crearVeterinario_ok() {
        VeterinarioRequestDTO request = requestBase();

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(repository.save(any(Veterinario.class)))
                .thenAnswer(invocation -> {
                    Veterinario veterinario = invocation.getArgument(0);
                    veterinario.setIdVeterinario(1);
                    return veterinario;
                });

        VeterinarioResponseDTO resultado = service.crear(request);

        assertNotNull(resultado);
        assertEquals(1, resultado.getIdVeterinario());
        assertEquals(request.getNombre().trim(), resultado.getNombre());
        assertEquals(request.getEspecialidad().trim(), resultado.getEspecialidad());
        assertEquals(request.getEmail(), resultado.getEmail());
        assertTrue(resultado.getActivo());

        verify(repository).findByEmail(request.getEmail());
        verify(repository).save(any(Veterinario.class));
    }

    @Test
    void crearVeterinario_emailDuplicado_lanzaExcepcion() {
        VeterinarioRequestDTO request = requestBase();
        Veterinario existente = veterinarioBase();

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(existente));

        assertThrows(
                DatoDuplicadoException.class,
                () -> service.crear(request)
        );

        verify(repository).findByEmail(request.getEmail());
        verify(repository, never()).save(any(Veterinario.class));
    }

    @Test
    void actualizarVeterinario_ok() {
        Veterinario existente = veterinarioBase();
        VeterinarioRequestDTO request = requestBase();
        request.setNombre("Maria Gonzalez");
        request.setEspecialidad("Dermatologia");
        request.setEmail("maria.gonzalez@veterinaria.cl");

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(repository.findById(1))
                .thenReturn(Optional.of(existente));

        when(repository.save(any(Veterinario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VeterinarioResponseDTO resultado = service.actualizar(1, request);

        assertNotNull(resultado);
        assertEquals("Maria Gonzalez", resultado.getNombre());
        assertEquals("Dermatologia", resultado.getEspecialidad());
        assertEquals("maria.gonzalez@veterinaria.cl", resultado.getEmail());

        verify(repository).findByEmail(request.getEmail());
        verify(repository).findById(1);
        verify(repository).save(any(Veterinario.class));
    }

    @Test
    void actualizarVeterinario_emailDuplicado_lanzaExcepcion() {
        VeterinarioRequestDTO request = requestBase();
        request.setEmail("repetido@veterinaria.cl");

        Veterinario otroVeterinario = Veterinario.builder()
                .idVeterinario(2)
                .nombre("Otro Veterinario")
                .especialidad("Medicina General")
                .telefono("+56922222222")
                .email("repetido@veterinaria.cl")
                .activo(true)
                .build();

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(otroVeterinario));

        assertThrows(
                DatoDuplicadoException.class,
                () -> service.actualizar(1, request)
        );

        verify(repository).findByEmail(request.getEmail());
        verify(repository, never()).save(any(Veterinario.class));
    }

    @Test
    void eliminarVeterinario_ok() {
        Veterinario veterinario = veterinarioBase();

        when(repository.findById(1))
                .thenReturn(Optional.of(veterinario));

        service.eliminar(1);

        verify(repository).findById(1);
        verify(repository).delete(veterinario);
    }

    private VeterinarioRequestDTO requestBase() {
        VeterinarioRequestDTO request = new VeterinarioRequestDTO();
        request.setNombre("Juan Perez");
        request.setEspecialidad("Cirugia");
        request.setTelefono("+56912345678");
        request.setEmail("juan.perez@veterinaria.cl");
        request.setActivo(true);
        return request;
    }

    private Veterinario veterinarioBase() {
        return Veterinario.builder()
                .idVeterinario(1)
                .nombre("Juan Perez")
                .especialidad("Cirugia")
                .telefono("+56912345678")
                .email("juan.perez@veterinaria.cl")
                .activo(true)
                .build();
    }
}