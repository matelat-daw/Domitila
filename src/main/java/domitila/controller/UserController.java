package domitila.controller;

import domitila.dto.RegisterRequestDTO;
import domitila.dto.UpdateUserPasswordRequestDTO;
import domitila.dto.UpdateUserRoleRequestDTO;
import domitila.dto.UserSummaryDTO;
import domitila.entity.Tecnico;
import domitila.service.TecnicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final TecnicoService tecnicoService;

    @GetMapping
    public ResponseEntity<Page<UserSummaryDTO>> obtenerUsuarios(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido1,
            @PageableDefault(size = 10, sort = {"nombre", "apellido1"}) Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(tecnicoService.obtenerUsuariosExcepto(authentication.getName(), nombre, apellido1, pageable));
    }

    @PostMapping
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO request) {
        Tecnico nuevoTecnico = Tecnico.builder()
                .nombre(request.nombre())
                .apellido1(request.apellido1())
                .apellido2(request.apellido2())
                .correoElectronico(request.correoElectronico())
                .clave(request.clave())
                .telefono(request.telefono())
                .dni(request.dni())
                .sexo(request.sexo())
                .fechaNacimiento(request.fechaNacimiento())
                .domicilioCompleto(request.domicilioCompleto())
                .numeroHijos(request.numeroHijos())
                .tipoJornada(request.tipoJornada())
                .horasJornadaParcial(request.horasJornadaParcial())
                .tipoContrato(request.tipoContrato())
                .grupoProfesional(request.grupoProfesional())
                .convenioLaboral(request.convenioLaboral())
                .numeroCuenta(request.numeroCuenta())
                .discapacidad(request.discapacidad())
                .fechaAlta(request.fechaAlta())
                .fechaBaja(request.fechaBaja())
                .salarioBruto(request.salarioBruto())
                .titulacion(request.titulacion())
                .vehiculo(request.vehiculo())
                .diasVacaciones(request.diasVacaciones())
                .idCategoriaProfesional(request.idCategoriaProfesional())
                .build();

        try {
            tecnicoService.registrarTecnico(nuevoTecnico);
        } catch (DataIntegrityViolationException ex) {
            String message = "El E-mail o el Teléfono ya está Registrado en la Base de Datos";
            String rawMessage = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
            if (rawMessage != null) {
                String normalized = rawMessage.toLowerCase();
                if (normalized.contains("correo_electronico") || normalized.contains("correo electronico") || normalized.contains("email")) {
                    message = "El correo electrónico ya está registrado en la base de datos";
                } else if (normalized.contains("telefono") || normalized.contains("teléfono")) {
                    message = "El Teléfono ya está Registrado en la Base de Datos";
                }
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } catch (ResponseStatusException ex) {
            String body = ex.getReason() != null ? ex.getReason() : "Error";
            return ResponseEntity.status(ex.getStatusCode()).body(body);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Técnico registrado exitosamente en el sistema");
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<String> actualizarRol(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRoleRequestDTO request,
            Authentication authentication
    ) {
        tecnicoService.actualizarRolUsuario(id, request.role(), authentication.getName());
        return ResponseEntity.ok("Rol actualizado exitosamente");
    }

    @PatchMapping("/me/password")
    public ResponseEntity<String> actualizarMiClave(
            @Valid @RequestBody UpdateUserPasswordRequestDTO request,
            Authentication authentication
    ) {
        tecnicoService.actualizarMiClave(authentication.getName(), request.nuevaClave());
        return ResponseEntity.ok("Clave actualizada exitosamente");
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<String> actualizarClaveUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserPasswordRequestDTO request
    ) {
        Integer idInt = id.intValue();
        tecnicoService.actualizarClaveUsuario(idInt, request.nuevaClave());
        return ResponseEntity.ok("Clave actualizada exitosamente");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> actualizar(@PathVariable Integer id, @RequestBody Tecnico request) {
        try {
            tecnicoService.actualizarTecnico(id, request);
            return ResponseEntity.ok("Técnico actualizado exitosamente en el sistema");
        } catch (DataIntegrityViolationException ex) {
            String message = "El E-mail o el Teléfono ya está Registrado en la Base de Datos";
            String rawMessage = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
            if (rawMessage != null) {
                String normalized = rawMessage.toLowerCase();
                if (normalized.contains("correo_electronico") || normalized.contains("correo electronico") || normalized.contains("email")) {
                    message = "El correo electrónico ya está registrado en la base de datos";
                } else if (normalized.contains("telefono") || normalized.contains("teléfono")) {
                    message = "El Teléfono ya está Registrado en la Base de Datos";
                }
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } catch (ResponseStatusException ex) {
            String body = ex.getReason() != null ? ex.getReason() : "Error";
            return ResponseEntity.status(ex.getStatusCode()).body(body);
        }
    }
}
