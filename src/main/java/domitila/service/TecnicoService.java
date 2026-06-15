package domitila.service;

import domitila.entity.Tecnico;
import domitila.repository.TecnicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

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
        return new domitila.security.TecnicoPrincipal(tecnico);
    }

    // --- MÉTODOS DEL CRUD ---

    // Crear (Guardar con contraseña encriptada)
    public Tecnico registrarTecnico(Tecnico tecnico) {
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
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado con ID: " + id));
    }

    // Actualizar
    public Tecnico actualizarTecnico(Long id, Tecnico datosActualizados) {
        Tecnico tecnicoExistente = obtenerPorId(id);
        
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
        if (tecnico != null) {
            tecnicoRepository.delete(tecnico);
        } else {
            throw new RuntimeException("Técnico no encontrado con ID: " + id);
        }
    }
}