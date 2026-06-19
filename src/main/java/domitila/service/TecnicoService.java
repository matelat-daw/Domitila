package domitila.service;

import domitila.dto.UserSummaryDTO;
import domitila.entity.RoleName;
import domitila.entity.Tecnico;
import domitila.repository.TecnicoRepository;
import domitila.security.TecnicoDetails;
import domitila.util.DocumentoIdentidadUtil;
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

    private final TecnicoRepository tecnicoRepository;
    private final PasswordEncoder passwordEncoder;

    // Método obligatorio para Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Tecnico tecnico = tecnicoRepository.findByCorreoElectronico(username)
                .orElseThrow(() -> new UsernameNotFoundException("Técnico no encontrado con username: " + username));
        return new TecnicoDetails(tecnico);
    }

    // --- MÉTODOS DEL CRUD ---

    // Crear (Guardar con contraseña encriptada)
    public Tecnico registrarTecnico(Tecnico tecnico) {
        tecnico.setNombre(normalizarTexto(tecnico.getNombre()));
        tecnico.setApellido1(normalizarTexto(tecnico.getApellido1()));
        tecnico.setApellido2(normalizarTextoOpcional(tecnico.getApellido2()));
        tecnico.setCorreoElectronico(normalizarTexto(tecnico.getCorreoElectronico()));
        tecnico.setTelefono(normalizarTextoOpcional(tecnico.getTelefono()));
        tecnico.setDni(normalizarDocumentoIdentidad(tecnico.getDni()));
        tecnico.setSexo(normalizarTextoOpcional(tecnico.getSexo()));
        tecnico.setDomicilioCompleto(normalizarTextoOpcional(tecnico.getDomicilioCompleto()));
        tecnico.setTipoJornada(normalizarTextoOpcional(tecnico.getTipoJornada()));
        tecnico.setTipoContrato(normalizarTextoOpcional(tecnico.getTipoContrato()));
        tecnico.setGrupoProfesional(normalizarTextoOpcional(tecnico.getGrupoProfesional()));
        tecnico.setConvenioLaboral(normalizarTextoOpcional(tecnico.getConvenioLaboral()));
        tecnico.setNumeroCuenta(normalizarTextoOpcional(tecnico.getNumeroCuenta()));
        tecnico.setTitulacion(normalizarTextoOpcional(tecnico.getTitulacion()));

        validarDniObligatorio(tecnico.getDni());

        if (tecnicoRepository.existsByCorreoElectronico(tecnico.getCorreoElectronico())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado en la base de datos");
        }
        if (tecnico.getTelefono() != null && tecnicoRepository.existsByTelefono(tecnico.getTelefono())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El Teléfono ya está Registrado en la Base de Datos");
        }

        if (tecnico.getRoles() == null) {
            tecnico.setRoles(new HashSet<>());
        }
        aplicarDefaultsRegistro(tecnico);
        tecnico.setClave(passwordEncoder.encode(tecnico.getClave()));
        return tecnicoRepository.save(tecnico);
    }

    // Leer todos
    public List<Tecnico> obtenerTodos() {
        return tecnicoRepository.findAll();
    }

    public Page<UserSummaryDTO> obtenerUsuariosExcepto(
            String correoElectronicoLogueado,
            String nombre,
            String apellido1,
            Pageable pageable
    ) {
        String nombreFiltro = normalizarFiltro(nombre);
        String apellido1Filtro = normalizarFiltro(apellido1);
        return tecnicoRepository.buscarUsuarios(correoElectronicoLogueado, nombreFiltro, apellido1Filtro, pageable)
                .map(this::toUserSummary);
    }

    // Leer por ID
    public Tecnico obtenerPorId(Integer id) {
        return tecnicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado con ID: " + id));
    }

    // Actualizar
    public Tecnico actualizarTecnico(Integer id, Tecnico datosActualizados) {
        Tecnico tecnicoExistente = obtenerPorId(id);
        
        // Verificar si el correo electrónico ya está en uso por otro técnico
        String correoElectronicoActualizado = normalizarTextoOpcional(datosActualizados.getCorreoElectronico());
        if (correoElectronicoActualizado != null && !correoElectronicoActualizado.isBlank()
            && !correoElectronicoActualizado.equalsIgnoreCase(tecnicoExistente.getCorreoElectronico())
            && tecnicoRepository.existsByCorreoElectronico(correoElectronicoActualizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado en la base de datos");
        }
        
        // Verificar si el telefono ya está en uso por otro técnico
        String telefonoActualizado = normalizarTextoOpcional(datosActualizados.getTelefono());
        if (telefonoActualizado != null
            && !telefonoActualizado.equals(tecnicoExistente.getTelefono())
            && tecnicoRepository.existsByTelefono(telefonoActualizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El Teléfono ya está Registrado en la Base de Datos");
        }
        
        String nombreActualizado = normalizarTextoOpcional(datosActualizados.getNombre());
        if (nombreActualizado != null && !nombreActualizado.isBlank()) {
            tecnicoExistente.setNombre(nombreActualizado);
        }

        String apellido1Actualizado = normalizarTextoOpcional(datosActualizados.getApellido1());
        if (apellido1Actualizado != null && !apellido1Actualizado.isBlank()) {
            tecnicoExistente.setApellido1(apellido1Actualizado);
        }

        if (datosActualizados.getApellido2() != null) {
            String apellido2Actualizado = normalizarTextoOpcional(datosActualizados.getApellido2());
            tecnicoExistente.setApellido2(apellido2Actualizado);
        }

        if (correoElectronicoActualizado != null) {
            tecnicoExistente.setCorreoElectronico(correoElectronicoActualizado);
        }

        if (datosActualizados.getTelefono() != null) {
            tecnicoExistente.setTelefono(telefonoActualizado);
        }

        String dniActualizado = normalizarDocumentoIdentidad(datosActualizados.getDni());
        if (dniActualizado != null && !dniActualizado.isBlank()) {
            validarDniObligatorio(dniActualizado);
            tecnicoExistente.setDni(dniActualizado);
        }

        String sexoActualizado = normalizarTextoOpcional(datosActualizados.getSexo());
        if (sexoActualizado != null && !sexoActualizado.isBlank()) {
            tecnicoExistente.setSexo(sexoActualizado);
        }

        if (datosActualizados.getFechaNacimiento() != null) {
            tecnicoExistente.setFechaNacimiento(datosActualizados.getFechaNacimiento());
        }

        if (datosActualizados.getDomicilioCompleto() != null) {
            tecnicoExistente.setDomicilioCompleto(normalizarTextoOpcional(datosActualizados.getDomicilioCompleto()));
        }

        if (datosActualizados.getNumeroHijos() != null) {
            tecnicoExistente.setNumeroHijos(datosActualizados.getNumeroHijos());
        }

        String tipoJornadaActualizado = normalizarTextoOpcional(datosActualizados.getTipoJornada());
        if (tipoJornadaActualizado != null && !tipoJornadaActualizado.isBlank()) {
            tecnicoExistente.setTipoJornada(tipoJornadaActualizado);
        }

        if (datosActualizados.getHorasJornadaParcial() != null) {
            tecnicoExistente.setHorasJornadaParcial(datosActualizados.getHorasJornadaParcial());
        }

        String tipoContratoActualizado = normalizarTextoOpcional(datosActualizados.getTipoContrato());
        if (tipoContratoActualizado != null && !tipoContratoActualizado.isBlank()) {
            tecnicoExistente.setTipoContrato(tipoContratoActualizado);
        }

        String grupoProfesionalActualizado = normalizarTextoOpcional(datosActualizados.getGrupoProfesional());
        if (grupoProfesionalActualizado != null && !grupoProfesionalActualizado.isBlank()) {
            tecnicoExistente.setGrupoProfesional(grupoProfesionalActualizado);
        }

        if (datosActualizados.getConvenioLaboral() != null) {
            tecnicoExistente.setConvenioLaboral(normalizarTextoOpcional(datosActualizados.getConvenioLaboral()));
        }

        if (datosActualizados.getNumeroCuenta() != null) {
            tecnicoExistente.setNumeroCuenta(normalizarTextoOpcional(datosActualizados.getNumeroCuenta()));
        }

        if (datosActualizados.getDiscapacidad() != null) {
            tecnicoExistente.setDiscapacidad(datosActualizados.getDiscapacidad());
        }

        if (datosActualizados.getFechaAlta() != null) {
            tecnicoExistente.setFechaAlta(datosActualizados.getFechaAlta());
        }

        if (datosActualizados.getFechaBaja() != null) {
            tecnicoExistente.setFechaBaja(datosActualizados.getFechaBaja());
        }

        if (datosActualizados.getSalarioBruto() != null) {
            tecnicoExistente.setSalarioBruto(datosActualizados.getSalarioBruto());
        }

        if (datosActualizados.getTitulacion() != null) {
            tecnicoExistente.setTitulacion(normalizarTextoOpcional(datosActualizados.getTitulacion()));
        }

        if (datosActualizados.getVehiculo() != null) {
            tecnicoExistente.setVehiculo(datosActualizados.getVehiculo());
        }

        if (datosActualizados.getDiasVacaciones() != null) {
            tecnicoExistente.setDiasVacaciones(datosActualizados.getDiasVacaciones());
        }

        if (datosActualizados.getIdCategoriaProfesional() != null) {
            tecnicoExistente.setIdCategoriaProfesional(datosActualizados.getIdCategoriaProfesional());
        }
        
        // Solo actualiza la clave si se envía una nueva en la petición
        if (datosActualizados.getClave() != null && !datosActualizados.getClave().isBlank()) {
            tecnicoExistente.setClave(passwordEncoder.encode(datosActualizados.getClave()));
        }

        return tecnicoRepository.save(tecnicoExistente);
    }

    // Eliminar
    public void eliminarTecnico(Integer id) {
        Tecnico tecnico = obtenerPorId(id);
        tecnicoRepository.delete(tecnico);
    }

    public void actualizarRolUsuario(Integer id, String role, String correoElectronicoLogueado) {
        Tecnico tecnico = obtenerPorId(id);
        if (tecnico.getCorreoElectronico().equalsIgnoreCase(correoElectronicoLogueado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes modificar tu propio rol");
        }

        if (role == null || role.isBlank() || "NONE".equalsIgnoreCase(role) || "SIN_ROL".equalsIgnoreCase(role)) {
            tecnico.setRoles(Set.of());
            tecnicoRepository.save(tecnico);
            return;
        }

        RoleName resolvedRole = parseRole(role);
        tecnico.setRoles(Set.of(resolvedRole));
        tecnicoRepository.save(tecnico);
    }

    public void actualizarClaveUsuario(Integer id, String nuevaClave) {
        Tecnico tecnico = obtenerPorId(id);
        tecnico.setClave(passwordEncoder.encode(nuevaClave));
        tecnicoRepository.save(tecnico);
    }

    public void actualizarMiClave(String correoElectronicoLogueado, String nuevaClave) {
        Tecnico tecnico = tecnicoRepository.findByCorreoElectronico(correoElectronicoLogueado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Técnico no encontrado con correo electrónico: " + correoElectronicoLogueado
                ));

        tecnico.setClave(passwordEncoder.encode(nuevaClave));
        tecnicoRepository.save(tecnico);
    }

    public UserSummaryDTO obtenerResumenUsuario(String correoElectronico) {
        Tecnico tecnico = tecnicoRepository.findByCorreoElectronico(correoElectronico)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Técnico no encontrado con correo electrónico: " + correoElectronico
                ));

        return toUserSummary(tecnico);
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
                        .map(role -> "ROLE_" + role.name())
                        .collect(java.util.stream.Collectors.toSet());

        return new UserSummaryDTO(
                tecnico.getId(),
                tecnico.getNombre(),
                tecnico.getApellido1(),
                tecnico.getApellido2(),
                tecnico.getCorreoElectronico(),
                tecnico.getTelefono(),
                roles
        );
    }

    private RoleName parseRole(String role) {
        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        try {
            return RoleName.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido: " + role);
        }
    }

    private void aplicarDefaultsRegistro(Tecnico tecnico) {
        if (tecnico.getSexo() == null) {
            tecnico.setSexo("No binario");
        }
        if (tecnico.getNumeroHijos() == null) {
            tecnico.setNumeroHijos(0);
        }
        if (tecnico.getTipoJornada() == null) {
            tecnico.setTipoJornada("Completa");
        }
        if (tecnico.getTipoContrato() == null) {
            tecnico.setTipoContrato("Temporal");
        }
        if (tecnico.getGrupoProfesional() == null) {
            tecnico.setGrupoProfesional("1");
        }
        if (tecnico.getDiscapacidad() == null) {
            tecnico.setDiscapacidad(false);
        }
        if (tecnico.getVehiculo() == null) {
            tecnico.setVehiculo(false);
        }
        if (tecnico.getIdCategoriaProfesional() == null) {
            tecnico.setIdCategoriaProfesional(1);
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null) {
            return null;
        }

        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }

    private String normalizarDocumentoIdentidad(String valor) {
        String normalizado = normalizarTextoOpcional(valor);
        if (normalizado == null) {
            return null;
        }

        return normalizado.toUpperCase().replaceAll("[-\\s]", "");
    }

    private void validarDniObligatorio(String dni) {
        if (dni == null || !DocumentoIdentidadUtil.validarDniNie(dni)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI/NIE no es válido");
        }
    }
}
