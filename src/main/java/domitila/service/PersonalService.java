package domitila.service;

import domitila.dto.UserSummaryDTO;
import domitila.entity.RoleName;
import domitila.entity.Personal;
import domitila.entity.Sexo;
import domitila.repository.PersonalRepository;
import domitila.security.PersonalDetails;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PersonalService implements UserDetailsService {

    private final PersonalRepository personalRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    // Método obligatorio para Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Personal personal = personalRepository.findByCorreoElectronico(username)
                .orElseThrow(() -> new UsernameNotFoundException("Personal no encontrado con username: " + username));
        return new PersonalDetails(personal);
    }

    // --- MÉTODOS DEL CRUD ---

    // Crear (Guardar con contraseña encriptada)
    public Personal registrarPersonal(Personal personal) {
        personal.setNombre(normalizarTexto(personal.getNombre()));
        personal.setApellido1(normalizarTexto(personal.getApellido1()));
        personal.setApellido2(normalizarTextoOpcional(personal.getApellido2()));
        personal.setCorreoElectronico(normalizarTexto(personal.getCorreoElectronico()));
        personal.setTelefono(normalizarTextoOpcional(personal.getTelefono()));
        personal.setDni(normalizarDocumentoIdentidad(personal.getDni()));
        personal.setDomicilioCompleto(normalizarTextoOpcional(personal.getDomicilioCompleto()));
        personal.setTipoJornada(normalizarTextoOpcional(personal.getTipoJornada()));
        personal.setTipoContrato(normalizarTextoOpcional(personal.getTipoContrato()));
        personal.setGrupoProfesional(normalizarTextoOpcional(personal.getGrupoProfesional()));
        personal.setConvenioLaboral(normalizarTextoOpcional(personal.getConvenioLaboral()));
        personal.setNumeroCuenta(normalizarTextoOpcional(personal.getNumeroCuenta()));
        personal.setTitulacion(normalizarTextoOpcional(personal.getTitulacion()));
        personal.setImagenPerfil(normalizarRutaImagenPerfil(personal.getImagenPerfil()));

        validarDniObligatorio(personal.getDni());

        if (personalRepository.existsByDni(personal.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI ya está registrado en la base de datos");
        }
        if (personalRepository.existsByCorreoElectronico(personal.getCorreoElectronico())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado en la base de datos");
        }
        if (personal.getTelefono() != null && personalRepository.existsByTelefono(personal.getTelefono())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El Teléfono ya está Registrado en la Base de Datos");
        }

        aplicarDefaultsPersonal(personal);
        personal.setClave(passwordEncoder.encode(personal.getClave()));
        return personalRepository.save(personal);
    }

    // Leer todos
    public List<Personal> obtenerTodos() {
        return personalRepository.findAll();
    }

    public Page<UserSummaryDTO> obtenerPersonalExcepto(
            String correoElectronicoLogueado,
            String nombre,
            String apellido1,
            Pageable pageable
    ) {
        String nombreFiltro = normalizarFiltro(nombre);
        String apellido1Filtro = normalizarFiltro(apellido1);
        return personalRepository.buscarPersonal(correoElectronicoLogueado, nombreFiltro, apellido1Filtro, pageable)
                .map(this::toUserSummary);
    }

    // Leer por ID
    public Personal obtenerPorId(Integer id) {
        return personalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personal no encontrado con ID: " + id));
    }

    // Actualizar
    public Personal actualizarPersonal(Integer id, Personal datosActualizados) {
        Personal personalExistente = obtenerPorId(id);
        
        // Verificar si el correo electrónico ya está en uso por otro personal diferente
        String correoElectronicoActualizado = normalizarTextoOpcional(datosActualizados.getCorreoElectronico());
        if (correoElectronicoActualizado != null && !correoElectronicoActualizado.isBlank()
            && !correoElectronicoActualizado.equalsIgnoreCase(personalExistente.getCorreoElectronico())
            && personalRepository.existsByCorreoElectronico(correoElectronicoActualizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado en la base de datos");
        }
        
        // Verificar si el telefono ya está en uso por otro personal diferente
        String telefonoActualizado = normalizarTextoOpcional(datosActualizados.getTelefono());
        if (telefonoActualizado != null
            && !telefonoActualizado.equals(personalExistente.getTelefono())
            && personalRepository.existsByTelefono(telefonoActualizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El Teléfono ya está Registrado en la Base de Datos");
        }
        
        String nombreActualizado = normalizarTextoOpcional(datosActualizados.getNombre());
        if (nombreActualizado != null && !nombreActualizado.isBlank()) {
            personalExistente.setNombre(nombreActualizado);
        }

        String apellido1Actualizado = normalizarTextoOpcional(datosActualizados.getApellido1());
        if (apellido1Actualizado != null && !apellido1Actualizado.isBlank()) {
            personalExistente.setApellido1(apellido1Actualizado);
        }

        if (datosActualizados.getApellido2() != null) {
            String apellido2Actualizado = normalizarTextoOpcional(datosActualizados.getApellido2());
            personalExistente.setApellido2(apellido2Actualizado);
        }

        if (correoElectronicoActualizado != null) {
            personalExistente.setCorreoElectronico(correoElectronicoActualizado);
        }

        if (datosActualizados.getTelefono() != null) {
            personalExistente.setTelefono(telefonoActualizado);
        }

        String dniActualizado = normalizarDocumentoIdentidad(datosActualizados.getDni());
        if (dniActualizado != null && !dniActualizado.isBlank()) {
            validarDniObligatorio(dniActualizado);
            if (!dniActualizado.equalsIgnoreCase(personalExistente.getDni())
                && personalRepository.existsByDni(dniActualizado)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI ya está registrado en la base de datos");
            }
            personalExistente.setDni(dniActualizado);
        }

        Sexo sexoActualizado = datosActualizados.getSexo();
        if (sexoActualizado != null) {
            personalExistente.setSexo(sexoActualizado);
        }

        if (datosActualizados.getFechaNacimiento() != null) {
            personalExistente.setFechaNacimiento(datosActualizados.getFechaNacimiento());
        }

        if (datosActualizados.getDomicilioCompleto() != null) {
            personalExistente.setDomicilioCompleto(normalizarTextoOpcional(datosActualizados.getDomicilioCompleto()));
        }

        if (datosActualizados.getNumeroHijos() != null) {
            personalExistente.setNumeroHijos(datosActualizados.getNumeroHijos());
        }

        String tipoJornadaActualizado = normalizarTextoOpcional(datosActualizados.getTipoJornada());
        if (tipoJornadaActualizado != null && !tipoJornadaActualizado.isBlank()) {
            personalExistente.setTipoJornada(tipoJornadaActualizado);
        }

        if (datosActualizados.getHorasJornadaParcial() != null) {
            personalExistente.setHorasJornadaParcial(datosActualizados.getHorasJornadaParcial());
        }

        String tipoContratoActualizado = normalizarTextoOpcional(datosActualizados.getTipoContrato());
        if (tipoContratoActualizado != null && !tipoContratoActualizado.isBlank()) {
            personalExistente.setTipoContrato(tipoContratoActualizado);
        }

        String grupoProfesionalActualizado = normalizarTextoOpcional(datosActualizados.getGrupoProfesional());
        if (grupoProfesionalActualizado != null && !grupoProfesionalActualizado.isBlank()) {
            personalExistente.setGrupoProfesional(grupoProfesionalActualizado);
        }

        if (datosActualizados.getConvenioLaboral() != null) {
            personalExistente.setConvenioLaboral(normalizarTextoOpcional(datosActualizados.getConvenioLaboral()));
        }

        if (datosActualizados.getNumeroCuenta() != null) {
            personalExistente.setNumeroCuenta(normalizarTextoOpcional(datosActualizados.getNumeroCuenta()));
        }

        if (datosActualizados.getDiscapacidad() != null) {
            personalExistente.setDiscapacidad(datosActualizados.getDiscapacidad());
        }

        if (datosActualizados.getFechaAlta() != null) {
            personalExistente.setFechaAlta(datosActualizados.getFechaAlta());
        }

        if (datosActualizados.getFechaBaja() != null) {
            personalExistente.setFechaBaja(datosActualizados.getFechaBaja());
        }

        if (datosActualizados.getSalarioBruto() != null) {
            personalExistente.setSalarioBruto(datosActualizados.getSalarioBruto());
        }

        if (datosActualizados.getTitulacion() != null) {
            personalExistente.setTitulacion(normalizarTextoOpcional(datosActualizados.getTitulacion()));
        }

        if (datosActualizados.getVehiculo() != null) {
            personalExistente.setVehiculo(datosActualizados.getVehiculo());
        }

        if (datosActualizados.getImagenPerfil() != null) {
            personalExistente.setImagenPerfil(normalizarRutaImagenPerfil(datosActualizados.getImagenPerfil()));
        }

        if (datosActualizados.getActivo() != null) {
            personalExistente.setActivo(datosActualizados.getActivo());
        }

        if (datosActualizados.getDiasVacaciones() != null) {
            personalExistente.setDiasVacaciones(datosActualizados.getDiasVacaciones());
        }

        if (datosActualizados.getIdCategoriaProfesional() != null) {
            personalExistente.setIdCategoriaProfesional(datosActualizados.getIdCategoriaProfesional());
        }
        
        // Solo actualiza la clave si se envía una nueva en la petición
        if (datosActualizados.getClave() != null && !datosActualizados.getClave().isBlank()) {
            personalExistente.setClave(passwordEncoder.encode(datosActualizados.getClave()));
        }

        return personalRepository.save(personalExistente);
    }

    // Eliminar
    public void eliminarPersonal(Integer id) {
        Personal personal = obtenerPorId(id);
        imageService.deleteImage(personal.getImagenPerfil());
        personalRepository.delete(personal);
    }

    public void actualizarRolPersonal(Integer id, String role, String action, String correoElectronicoLogueado) {
        Personal personal = obtenerPorId(id);
        if (personal.getCorreoElectronico().equalsIgnoreCase(correoElectronicoLogueado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes modificar tu propio rol");
        }

        RoleName resolvedRole = parseRole(role);
        String resolvedAction = parseRoleAction(action);
        Set<RoleName> rolesActuales = personal.getRoles() == null
                ? new HashSet<>()
                : new HashSet<>(personal.getRoles());

        if ("ADD".equals(resolvedAction)) {
            rolesActuales.add(resolvedRole);
        } else {
            rolesActuales.remove(resolvedRole);
        }

        personal.setRoles(rolesActuales);
        personalRepository.save(personal);
    }

    public void actualizarClavePersonal(Integer id, String nuevaClave) {
        Personal personal = obtenerPorId(id);
        personal.setClave(passwordEncoder.encode(nuevaClave));
        personalRepository.save(personal);
    }

    public String actualizarImagenPerfil(Integer id, MultipartFile file) {
        Personal personal = obtenerPorId(id);
        String imagenAnterior = personal.getImagenPerfil();
        String nuevaRuta = imageService.saveProfileImage(file, id);

        if (imagenAnterior != null && !imagenAnterior.equals(nuevaRuta)) {
            imageService.deleteImage(imagenAnterior);
        }

        personal.setImagenPerfil(nuevaRuta);
        personalRepository.save(personal);
        return nuevaRuta;
    }

    public String actualizarRutaImagenPerfil(Integer id, String imagenPerfil) {
        Personal personal = obtenerPorId(id);
        String rutaNormalizada = normalizarRutaImagenPerfil(imagenPerfil);
        personal.setImagenPerfil(rutaNormalizada);
        personalRepository.save(personal);
        return rutaNormalizada;
    }

    public void actualizarMiClave(String correoElectronicoLogueado, String nuevaClave) {
        Personal personal = personalRepository.findByCorreoElectronico(correoElectronicoLogueado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Personal no encontrado con correo electrónico: " + correoElectronicoLogueado
                ));

        personal.setClave(passwordEncoder.encode(nuevaClave));
        personalRepository.save(personal);
    }

    public UserSummaryDTO obtenerResumenPersonal(String correoElectronico) {
        Personal personal = personalRepository.findByCorreoElectronico(correoElectronico)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Personal no encontrado con correo electrónico: " + correoElectronico
                ));

        return toUserSummary(personal);
    }

    private String normalizarFiltro(String valor) {
        if (valor == null) {
            return null;
        }

        String valorNormalizado = valor.trim();
        return valorNormalizado.isEmpty() ? null : valorNormalizado;
    }

    private UserSummaryDTO toUserSummary(Personal personal) {
        Set<String> roles = personal.getRoles() == null
                ? Set.of()
                : personal.getRoles().stream()
                        .map(role -> "ROLE_" + role.name())
                        .collect(java.util.stream.Collectors.toSet());

        return new UserSummaryDTO(
                personal.getId(),
                personal.getNombre(),
                personal.getApellido1(),
                personal.getApellido2(),
                personal.getCorreoElectronico(),
                personal.getTelefono(),
                roles,
                personal.getImagenPerfil(),
                personal.getActivo()
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

    private String parseRoleAction(String action) {
        String normalized = action == null ? "" : action.trim().toUpperCase();
        if ("ADD".equals(normalized) || "REMOVE".equals(normalized)) {
            return normalized;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Acción de rol inválida: " + action);
    }

    private void aplicarDefaultsPersonal(Personal personal) {
        if (personal.getSexo() == null) {
            personal.setSexo(Sexo.NO_BINARIO);
        }
        if (personal.getNumeroHijos() == null) {
            personal.setNumeroHijos(0);
        }
        if (personal.getTipoJornada() == null) {
            personal.setTipoJornada("Completa");
        }
        if (personal.getTipoContrato() == null) {
            personal.setTipoContrato("Temporal");
        }
        if (personal.getGrupoProfesional() == null) {
            personal.setGrupoProfesional("1");
        }
        if (personal.getDiscapacidad() == null) {
            personal.setDiscapacidad(false);
        }
        if (personal.getVehiculo() == null) {
            personal.setVehiculo(false);
        }
        if (personal.getActivo() == null) {
            personal.setActivo(true);
        }
        if (personal.getIdCategoriaProfesional() == null) {
            personal.setIdCategoriaProfesional(1);
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

    private String normalizarRutaImagenPerfil(String valor) {
        String normalizado = normalizarTextoOpcional(valor);
        if (normalizado == null) {
            return null;
        }

        String rutaNormalizada = normalizado.replace("\\", "/").replaceAll("/{2,}", "/");
        if (!imageService.isValidImagePath(rutaNormalizada)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ruta de la imagen de perfil no es válida.");
        }

        return rutaNormalizada;
    }

    private void validarDniObligatorio(String dni) {
        if (dni == null || !DocumentoIdentidadUtil.validarDniNie(dni)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI/NIE no es correcto. Revisa el número y la letra.");
        }
    }
}
