package domitila.service;

import domitila.dto.UserSummaryDTO;
import domitila.entity.Role;
import domitila.entity.Tecnico;
import domitila.repository.RoleRepository;
import domitila.repository.TecnicoRepository;
import domitila.security.TecnicoDetails;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Set;

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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El E-mail ya está Registrado en la Base de Datos");
        }
        if (tecnicoRepository.existsByTelefono(tecnico.getTelefono())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El Teléfono ya está Registrado en la Base de Datos");
        }

        if (tecnico.getRoles() == null) {
            tecnico.setRoles(new HashSet<>());
        }
        if (tecnico.getRoles().isEmpty()) {
            tecnico.setRoles(Set.of(resolveRoleByName(DEFAULT_REGISTER_ROLE)));
        }
        tecnico.setClave(passwordEncoder.encode(tecnico.getClave()));
        return tecnicoRepository.save(tecnico);
    }

    // Leer todos
    public List<Tecnico> obtenerTodos() {
        return tecnicoRepository.findAll();
    }

    public Page<UserSummaryDTO> obtenerUsuariosExcepto(
            String emailLogueado,
            String nombre,
            String apellido1,
            Pageable pageable
    ) {
        String nombreFiltro = normalizarFiltro(nombre);
        String apellido1Filtro = normalizarFiltro(apellido1);
        return tecnicoRepository.buscarUsuarios(emailLogueado, nombreFiltro, apellido1Filtro, pageable)
                .map(this::toUserSummary);
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
            && !emailActualizado.equalsIgnoreCase(tecnicoExistente.getEmail())
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

    public void actualizarRolUsuario(Long id, Long roleId, String emailLogueado) {
        Tecnico tecnico = obtenerPorId(id);
        if (tecnico.getEmail().equalsIgnoreCase(emailLogueado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes modificar tu propio rol");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado con ID: " + roleId));

        tecnico.setRoles(Set.of(role));
        tecnicoRepository.save(tecnico);
    }

    public void actualizarClaveUsuario(Long id, String nuevaClave) {
        Tecnico tecnico = obtenerPorId(id);
        tecnico.setClave(passwordEncoder.encode(nuevaClave));
        tecnicoRepository.save(tecnico);
    }

    public void actualizarMiClave(String emailLogueado, String nuevaClave) {
        Tecnico tecnico = tecnicoRepository.findByEmail(emailLogueado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Técnico no encontrado con email: " + emailLogueado
                ));

        tecnico.setClave(passwordEncoder.encode(nuevaClave));
        tecnicoRepository.save(tecnico);
    }

    public UserSummaryDTO obtenerResumenUsuario(String email) {
        Tecnico tecnico = tecnicoRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Técnico no encontrado con email: " + email
                ));

        return toUserSummary(tecnico);
    }

    private Role resolveRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .or(() -> roleRepository.findByName("ROLE_" + roleName))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No existe el rol " + roleName + " configurado en la base de datos"
                ));
    }

    private String normalizarFiltro(String valor) {
        if (valor == null) {
            return null;
        }

        String valorNormalizado = valor.trim();
        return valorNormalizado.isEmpty() ? null : valorNormalizado;
    }

    private UserSummaryDTO toUserSummary(Tecnico tecnico) {
        Set<String> roles = tecnico.getRoles() == null
                ? Set.of()
                : tecnico.getRoles().stream()
                        .map(Role::getName)
                        .collect(java.util.stream.Collectors.toSet());

        return new UserSummaryDTO(
                tecnico.getId(),
                tecnico.getNombre(),
                tecnico.getApellido1(),
                tecnico.getApellido2(),
                tecnico.getEmail(),
                tecnico.getTelefono(),
                roles
        );
    }
}
