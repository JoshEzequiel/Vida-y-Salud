package com.vidaysalud.veterinaria.mascotaservicio.service;
import com.vidaysalud.veterinaria.mascotaservicio.client.ClienteClient; import com.vidaysalud.veterinaria.mascotaservicio.dto.*; import com.vidaysalud.veterinaria.mascotaservicio.exception.*; import com.vidaysalud.veterinaria.mascotaservicio.model.Mascota; import com.vidaysalud.veterinaria.mascotaservicio.repository.MascotaRepository; import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.List;
@Service @RequiredArgsConstructor @Slf4j @Transactional
public class MascotaService{
 private final MascotaRepository repository; private final ClienteClient clienteClient;
 @Transactional(readOnly=true) public List<MascotaResponseDTO> listar(){return repository.findAll().stream().map(this::dto).toList();}
 @Transactional(readOnly=true) public MascotaResponseDTO buscar(Integer id){return dto(obtener(id));}
 @Transactional(readOnly=true) public List<MascotaResponseDTO> porCliente(Integer id){return repository.findByIdCliente(id).stream().map(this::dto).toList();}
 public MascotaResponseDTO crear(MascotaRequestDTO d){clienteClient.validar(d.getIdCliente()); Mascota m=map(new Mascota(),d);m=repository.save(m);log.info("Mascota creada id={}",m.getIdMascota());return dto(m);}
 public MascotaResponseDTO actualizar(Integer id,MascotaRequestDTO d){clienteClient.validar(d.getIdCliente());Mascota m=map(obtener(id),d);return dto(repository.save(m));}
 public void eliminar(Integer id){repository.delete(obtener(id));}
 private Mascota obtener(Integer id){return repository.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Mascota no encontrada con id: "+id));}
 private Mascota map(Mascota m,MascotaRequestDTO d){m.setIdCliente(d.getIdCliente());m.setNombre(d.getNombre().trim());m.setEspecie(d.getEspecie().trim());m.setRaza(d.getRaza());m.setFechaNacimiento(d.getFechaNacimiento());m.setSexo(d.getSexo());m.setPeso(d.getPeso());m.setActiva(d.getActiva()==null?true:d.getActiva());return m;}
 private MascotaResponseDTO dto(Mascota m){return MascotaResponseDTO.builder().idMascota(m.getIdMascota()).idCliente(m.getIdCliente()).nombre(m.getNombre()).especie(m.getEspecie()).raza(m.getRaza()).fechaNacimiento(m.getFechaNacimiento()).sexo(m.getSexo()).peso(m.getPeso()).activa(m.getActiva()).build();}
}
