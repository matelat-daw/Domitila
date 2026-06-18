package domitila.service;

import domitila.entity.Tecnico;
import domitila.repository.TecnicoRepository;
import domitila.security.TecnicoDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class TecnicoService implements UserDetailsService {

    private final TecnicoRepository tecnicoRepository;
    private final PasswordEncoder passwordEncoder;

    // Método obligatorio para Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Tecnico tecnico = tecnicoRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Técnico no encontrado con username: " + username));
        return new TecnicoDetails(tecnico);
    }

    // --- MÉTODOS DEL CRUD ---

    // Crear (Guardar con contraseña encriptada)
    public Tecnico registrarTecnico(Tecnico tecnico) {
        if (tecnicoRepository.existsByEmail(tecnico.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El E-mail ya está Registrado en la Base de Datos");
        }
        if (tecnicoRepository.existsByTelefono(tecnico.getTelefono())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El Teléfono ya está Registrado en la Base de Datos");
        }

        if (tecnico.getRoles() == null) {
            tecnico.setRoles(new HashSet<>());
        }
        tecnico.setClave(passwordEncoder.encode(tecnico.getClave()));
        return tecnicoRepository.save(tecnico);
    }

    // Leer todos
    public List<Tecnico> obtenerTodos() {
        return tecnicoRepository.findAll();
    }

    // Leer por ID
    public Tecnico obtenerPorId(Long id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado con ID: " + id));
    }

    // Actualizar
    public Tecnico actualizarTecnico(Long id, Tecnico datosActualizados) {
        Tecnico tecnicoExistente = obtenerPorId(id);
        
        // Verificar si el email ya está en uso por otro técnico
        String emailActualizado = datosActualizados.getEmail();
        if (emailActualizado != null && !emailActualizado.isBlank()
            && !emailActualizado.equals(tecnicoExistente.getEmail())
            && tecnicoRepository.existsByEmail(emailActualizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El E-mail ya está Registrado en la Base de Datos");
        }
        
        // Verificar si el telefono ya está en uso por otro técnico
        String telefonoActualizado = datosActualizados.getTelefono();
        if (telefonoActualizado != null && !telefonoActualizado.isBlank()
            && !telefonoActualizado.equals(tecnicoExistente.getTelefono())
            && tecnicoRepository.existsByTelefono(telefonoActualizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El Teléfono ya está Registrado en la Base de Datos");
        }
        
        String nombreActualizado = datosActualizados.getNombre();
        if (nombreActualizado != null && !nombreActualizado.isBlank()) {
            tecnicoExistente.setNombre(nombreActualizado);
        }

        String apellido1Actualizado = datosActualizados.getApellido1();
        if (apellido1Actualizado != null && !apellido1Actualizado.isBlank()) {
            tecnicoExistente.setApellido1(apellido1Actualizado);
        }

        String apellido2Actualizado = datosActualizados.getApellido2();
        if (apellido2Actualizado != null && !apellido2Actualizado.isBlank()) {
            tecnicoExistente.setApellido2(apellido2Actualizado);
        }

        if (emailActualizado != null && !emailActualizado.isBlank()) {
            tecnicoExistente.setEmail(emailActualizado);
        }

        if (telefonoActualizado != null && !telefonoActualizado.isBlank()) {
            tecnicoExistente.setTelefono(telefonoActualizado);
        }
        
        // Solo actualiza la clave si se envía una nueva en la petición
        if (datosActualizados.getClave() != null && !datosActualizados.getClave().isBlank()) {
            tecnicoExistente.setClave(passwordEncoder.encode(datosActualizados.getClave()));
        }

        return tecnicoRepository.save(tecnicoExistente);
    }

    // Eliminar
    public void eliminarTecnico(Long id) {
        Tecnico tecnico = obtenerPorId(id);
        tecnicoRepository.delete(tecnico);
    }
}
