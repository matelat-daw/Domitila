package domitila.service;

import domitila.entity.Tecnico;
import domitila.entity.Role;
import domitila.repository.RoleRepository;
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

@Service
@RequiredArgsConstructor
public class TecnicoService implements UserDetailsService {

    private static final String DEFAULT_REGISTER_ROLE = "TECNICO";

    private final RoleRepository roleRepository;
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }
        if (tecnicoRepository.existsByTelefono(tecnico.getTelefono())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El telefono ya está registrado");
        }

        tecnico.setRole(resolveTecnicoRole());
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
        if (!tecnicoExistente.getEmail().equals(datosActualizados.getEmail()) &&
            tecnicoRepository.existsByEmail(datosActualizados.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }
        
        // Verificar si el telefono ya está en uso por otro técnico
        if (!tecnicoExistente.getTelefono().equals(datosActualizados.getTelefono()) &&
            tecnicoRepository.existsByTelefono(datosActualizados.getTelefono())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El telefono ya está registrado");
        }
        
        tecnicoExistente.setNombre(datosActualizados.getNombre());
        tecnicoExistente.setEmail(datosActualizados.getEmail());
        tecnicoExistente.setTelefono(datosActualizados.getTelefono());
        
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

    private Role resolveTecnicoRole() {
        return roleRepository.findByName(DEFAULT_REGISTER_ROLE)
                .or(() -> roleRepository.findByName("ROLE_" + DEFAULT_REGISTER_ROLE))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No existe el rol TECNICO configurado en la base de datos"
                ));
    }
}