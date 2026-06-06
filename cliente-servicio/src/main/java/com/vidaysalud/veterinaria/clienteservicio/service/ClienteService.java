package com.vidaysalud.veterinaria.clienteservicio.service;
import com.vidaysalud.veterinaria.clienteservicio.dto.*; import com.vidaysalud.veterinaria.clienteservicio.exception.*; import com.vidaysalud.veterinaria.clienteservicio.model.Cliente; import com.vidaysalud.veterinaria.clienteservicio.repository.ClienteRepository;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.List;
@Service @RequiredArgsConstructor @Slf4j @Transactional
public class ClienteService {
 private final ClienteRepository repository;
 @Transactional(readOnly=true) public List<ClienteResponseDTO> listar(){ log.info("Listando clientes"); return repository.findAll().stream().map(this::toDto).toList(); }
 @Transactional(readOnly=true) public ClienteResponseDTO buscarPorId(Integer id){ return toDto(obtener(id)); }
 @Transactional(readOnly=true) public ClienteResponseDTO buscarPorRut(String rut){ String r=trim(rut); return toDto(repository.findByRut(r).orElseThrow(()->new RecursoNoEncontradoException("Cliente no encontrado con RUT: "+r))); }
 public ClienteResponseDTO crear(ClienteRequestDTO d){ String r=trim(d.getRut()), e=opt(d.getEmail()); validar(r,e,null); Cliente c=Cliente.builder().rut(r).nombre(trim(d.getNombre())).telefono(opt(d.getTelefono())).email(e).direccion(opt(d.getDireccion())).build(); c=repository.save(c); log.info("Cliente creado id={}",c.getIdCliente()); return toDto(c); }
 public ClienteResponseDTO actualizar(Integer id,ClienteRequestDTO d){ Cliente c=obtener(id); String r=trim(d.getRut()),e=opt(d.getEmail()); validar(r,e,id); c.setRut(r);c.setNombre(trim(d.getNombre()));c.setTelefono(opt(d.getTelefono()));c.setEmail(e);c.setDireccion(opt(d.getDireccion())); return toDto(repository.save(c)); }
 public void eliminar(Integer id){ repository.delete(obtener(id)); log.info("Cliente eliminado id={}",id); }
 private Cliente obtener(Integer id){ return repository.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Cliente no encontrado con id: "+id)); }
 private void validar(String rut,String email,Integer id){ repository.findByRut(rut).filter(x->!x.getIdCliente().equals(id)).ifPresent(x->{throw new DatoDuplicadoException("Ya existe un cliente con el RUT: "+rut);}); if(email!=null) repository.findByEmail(email).filter(x->!x.getIdCliente().equals(id)).ifPresent(x->{throw new DatoDuplicadoException("Ya existe un cliente con el correo: "+email);}); }
 private ClienteResponseDTO toDto(Cliente c){ return ClienteResponseDTO.builder().idCliente(c.getIdCliente()).rut(c.getRut()).nombre(c.getNombre()).telefono(c.getTelefono()).email(c.getEmail()).direccion(c.getDireccion()).fechaRegistro(c.getFechaRegistro()).build(); }
 private String trim(String s){return s==null?null:s.trim();} private String opt(String s){return s==null||s.isBlank()?null:s.trim();}
}
