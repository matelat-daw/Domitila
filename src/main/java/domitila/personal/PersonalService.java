package domitila.personal;

import domitila.auth.dto.RegisterRequestDTO;
import domitila.auth.dto.UserSummaryDTO;
import domitila.auth.enums.RoleName;
import domitila.auth.security.PersonalDetails;
import domitila.auth.util.DocumentoIdentidadUtil;
import domitila.auth.service.ImageService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PersonalService implements UserDetailsService {

    private final PersonalRepository personalRepository;
    private final GeneroCatalogoRepository generoCatalogoRepository;
    private final TipoJornadaCatalogoRepository tipoJornadaCatalogoRepository;
    private final TipoContratoCatalogoRepository tipoContratoCatalogoRepository;
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
    public Personal registrarPersonal(RegisterRequestDTO request) {
        Personal personal = Personal.builder()
                .nombre(request.nombre())
                .apellido1(request.apellido1())
                .apellido2(request.apellido2())
                .correoElectronico(request.correoElectronico())
                .telefono(request.telefono())
                .dni(request.dni())
                .genero(resolveGenero(request.generoId()))
                .fechaNacimiento(request.fechaNacimiento())
                .domicilioCompleto(request.domicilioCompleto())
                .numeroHijos(request.numeroHijos())
                .tipoJornada(resolveTipoJornada(request.tipoJornadaId()))
                .horasJornadaParcial(request.horasJornadaParcial())
                .tipoContrato(resolveTipoContrato(request.tipoContratoId()))
                .grupoProfesional(request.grupoProfesional())
                .convenioLaboral(request.convenioLaboral())
                .numeroCuenta(request.numeroCuenta())
                .discapacidad(request.discapacidad())
                .fechaAlta(request.fechaAlta())
                .fechaBaja(request.fechaBaja())
                .salarioBruto(request.salarioBruto())
                .titulacion(request.titulacion())
                .vehiculo(request.vehiculo())
                .imagenPerfil(request.imagenPerfil())
                .activo(request.activo())
                .diasVacaciones(request.diasVacaciones())
                .idCategoriaProfesional(request.idCategoriaProfesional())
                .build();

        personal.setNombre(normalizarTexto(personal.getNombre()));
        personal.setApellido1(normalizarTexto(personal.getApellido1()));
        personal.setApellido2(normalizarTextoOpcional(personal.getApellido2()));
        personal.setCorreoElectronico(normalizarTexto(personal.getCorreoElectronico()));
        personal.setTelefono(normalizarTextoOpcional(personal.getTelefono()));
        personal.setDni(normalizarDocumentoIdentidad(personal.getDni()));
        personal.setDomicilioCompleto(normalizarTextoOpcional(personal.getDomicilioCompleto()));
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
        // La clave inicial siempre se genera a partir del DNI ya validado y normalizado.
        personal.setClave(passwordEncoder.encode(personal.getDni()));
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
    public Personal actualizarPersonal(Integer id, UpdatePersonalRequestDTO request) {
        Personal personalExistente = obtenerPorId(id);
        
        // Verificar si el correo electrónico ya está en uso por otro personal diferente
        String correoElectronicoActualizado = normalizarTextoOpcional(request.correoElectronico());
        if (correoElectronicoActualizado != null && !correoElectronicoActualizado.isBlank()
            && !correoElectronicoActualizado.equalsIgnoreCase(personalExistente.getCorreoElectronico())
            && personalRepository.existsByCorreoElectronico(correoElectronicoActualizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado en la base de datos");
        }
        
        // Verificar si el telefono ya está en uso por otro personal diferente
        String telefonoActualizado = normalizarTextoOpcional(request.telefono());
        if (telefonoActualizado != null
            && !telefonoActualizado.equals(personalExistente.getTelefono())
            && personalRepository.existsByTelefono(telefonoActualizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El Teléfono ya está Registrado en la Base de Datos");
        }
        
        String nombreActualizado = normalizarTextoOpcional(request.nombre());
        if (nombreActualizado != null && !nombreActualizado.isBlank()) {
            personalExistente.setNombre(nombreActualizado);
        }

        String apellido1Actualizado = normalizarTextoOpcional(request.apellido1());
        if (apellido1Actualizado != null && !apellido1Actualizado.isBlank()) {
            personalExistente.setApellido1(apellido1Actualizado);
        }

        if (request.apellido2() != null) {
            String apellido2Actualizado = normalizarTextoOpcional(request.apellido2());
            personalExistente.setApellido2(apellido2Actualizado);
        }

        if (correoElectronicoActualizado != null) {
            personalExistente.setCorreoElectronico(correoElectronicoActualizado);
        }

        if (request.telefono() != null) {
            personalExistente.setTelefono(telefonoActualizado);
        }

        String dniActualizado = normalizarDocumentoIdentidad(request.dni());
        if (dniActualizado != null && !dniActualizado.isBlank()) {
            validarDniObligatorio(dniActualizado);
            if (!dniActualizado.equalsIgnoreCase(personalExistente.getDni())
                && personalRepository.existsByDni(dniActualizado)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El DNI ya está registrado en la base de datos");
            }
            personalExistente.setDni(dniActualizado);
        }

        if (request.generoId() != null) {
            personalExistente.setGenero(resolveGenero(request.generoId()));
        }

        if (request.fechaNacimiento() != null) {
            personalExistente.setFechaNacimiento(request.fechaNacimiento());
        }

        if (request.domicilioCompleto() != null) {
            personalExistente.setDomicilioCompleto(normalizarTextoOpcional(request.domicilioCompleto()));
        }

        if (request.numeroHijos() != null) {
            personalExistente.setNumeroHijos(request.numeroHijos());
        }

        if (request.tipoJornadaId() != null) {
            personalExistente.setTipoJornada(resolveTipoJornada(request.tipoJornadaId()));
        }

        if (request.horasJornadaParcial() != null) {
            personalExistente.setHorasJornadaParcial(request.horasJornadaParcial());
        }

        if (request.tipoContratoId() != null) {
            personalExistente.setTipoContrato(resolveTipoContrato(request.tipoContratoId()));
        }

        GrupoProfesional grupoProfesionalActualizado = request.grupoProfesional();
        if (grupoProfesionalActualizado != null) {
            personalExistente.setGrupoProfesional(grupoProfesionalActualizado);
        }

        ConvenioLaboral convenioLaboralActualizado = request.convenioLaboral();
        if (convenioLaboralActualizado != null) {
            personalExistente.setConvenioLaboral(convenioLaboralActualizado);
        }

        if (request.numeroCuenta() != null) {
            personalExistente.setNumeroCuenta(normalizarTextoOpcional(request.numeroCuenta()));
        }

        if (request.discapacidad() != null) {
            personalExistente.setDiscapacidad(request.discapacidad());
        }

        if (request.fechaAlta() != null) {
            personalExistente.setFechaAlta(request.fechaAlta());
        }

        if (request.fechaBaja() != null) {
            personalExistente.setFechaBaja(request.fechaBaja());
        }

        if (request.salarioBruto() != null) {
            personalExistente.setSalarioBruto(request.salarioBruto());
        }

        if (request.titulacion() != null) {
            personalExistente.setTitulacion(normalizarTextoOpcional(request.titulacion()));
        }

        if (request.vehiculo() != null) {
            personalExistente.setVehiculo(request.vehiculo());
        }

        if (request.imagenPerfil() != null) {
            personalExistente.setImagenPerfil(normalizarRutaImagenPerfil(request.imagenPerfil()));
        }

        if (request.activo() != null) {
            personalExistente.setActivo(request.activo());
        }

        if (request.diasVacaciones() != null) {
            personalExistente.setDiasVacaciones(request.diasVacaciones());
        }

        if (request.idCategoriaProfesional() != null) {
            personalExistente.setIdCategoriaProfesional(request.idCategoriaProfesional());
        }
        
        // Solo actualiza la clave si se envía una nueva en la petición
        return personalRepository.save(personalExistente);
    }

    // Eliminar
    @Transactional
    public void eliminarPersonal(Integer id) {
        Personal personal = obtenerPorId(id);
        String imagenPerfil = personal.getImagenPerfil();

        if (personal.getRoles() != null) {
            personal.getRoles().clear();
        }
        if (personal.getProyectos() != null) {
            personal.getProyectos().clear();
        }
        personal.setImagenPerfil(null);

        personalRepository.save(personal);
        personalRepository.flush();
        personalRepository.delete(personal);
        personalRepository.flush();

        imageService.deleteImage(imagenPerfil);
        imageService.deletePersonalDirectory(id);
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

    public void actualizarEstadoPersonal(Integer id, Boolean activo) {
        Personal personal = obtenerPorId(id);
        personal.setActivo(Boolean.TRUE.equals(activo));
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

    public String actualizarMiImagenPerfil(String correoElectronicoLogueado, MultipartFile file) {
        Personal personal = obtenerPorCorreoElectronico(correoElectronicoLogueado);
        String imagenAnterior = personal.getImagenPerfil();
        String nuevaRuta = imageService.saveProfileImage(file, personal.getId());

        if (imagenAnterior != null && !imagenAnterior.equals(nuevaRuta)) {
            imageService.deleteImage(imagenAnterior);
        }

        personal.setImagenPerfil(nuevaRuta);
        personalRepository.save(personal);
        return nuevaRuta;
    }

    public void actualizarMiClave(String correoElectronicoLogueado, String nuevaClave) {
        Personal personal = obtenerPorCorreoElectronico(correoElectronicoLogueado);
        personal.setClave(passwordEncoder.encode(nuevaClave));
        personalRepository.save(personal);
    }

    public UserSummaryDTO obtenerResumenPersonal(String correoElectronico) {
        Personal personal = obtenerPorCorreoElectronico(correoElectronico);
        return toUserSummary(personal);
    }

    private Personal obtenerPorCorreoElectronico(String correoElectronico) {
        return personalRepository.findByCorreoElectronico(correoElectronico)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Personal no encontrado con correo electrónico: " + correoElectronico
                ));
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
                personal.getGenero() == null ? null : personal.getGenero().getId(),
                personal.getGenero() == null ? null : personal.getGenero().getNombre(),
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
        if (personal.getGenero() == null) {
            personal.setGenero(resolveGeneroPorNombre("No Binario"));
        }
        if (personal.getNumeroHijos() == null) {
            personal.setNumeroHijos(0);
        }
        if (personal.getTipoJornada() == null) {
            personal.setTipoJornada(resolveTipoJornadaPorNombre("Completa"));
        }
        if (personal.getTipoContrato() == null) {
            personal.setTipoContrato(resolveTipoContratoPorNombre("Temporal"));
        }
        if (personal.getGrupoProfesional() == null) {
            personal.setGrupoProfesional(GrupoProfesional.GRUPO_1);
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

    private GeneroCatalogo resolveGenero(Integer generoId) {
        return generoCatalogoRepository.findById(generoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El género seleccionado no existe."));
    }

    private TipoJornadaCatalogo resolveTipoJornada(Integer tipoJornadaId) {
        return tipoJornadaCatalogoRepository.findById(tipoJornadaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de jornada seleccionado no existe."));
    }

    private TipoContratoCatalogo resolveTipoContrato(Integer tipoContratoId) {
        return tipoContratoCatalogoRepository.findById(tipoContratoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de contrato seleccionado no existe."));
    }

    private GeneroCatalogo resolveGeneroPorNombre(String nombre) {
        return generoCatalogoRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No existe el género por defecto configurado en base de datos."));
    }

    private TipoJornadaCatalogo resolveTipoJornadaPorNombre(String nombre) {
        return tipoJornadaCatalogoRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No existe el tipo de jornada por defecto configurado en base de datos."));
    }

    private TipoContratoCatalogo resolveTipoContratoPorNombre(String nombre) {
        return tipoContratoCatalogoRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No existe el tipo de contrato por defecto configurado en base de datos."));
    }
}
