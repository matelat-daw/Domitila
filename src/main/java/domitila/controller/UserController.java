package domitila.controller;

import domitila.dto.RegisterRequestDTO;
import domitila.dto.UpdateUserPasswordRequestDTO;
import domitila.dto.UpdateUserRoleRequestDTO;
import domitila.dto.UserSummaryDTO;
import domitila.entity.Tecnico;
import domitila.service.TecnicoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final TecnicoService tecnicoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryDTO>> obtenerUsuarios(Authentication authentication) {
        return ResponseEntity.ok(tecnicoService.obtenerUsuariosExcepto(authentication.getName()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO request) {
        Tecnico nuevoTecnico = Tecnico.builder()
                .nombre(request.getNombre())
                .apellido1(request.getApellido1())
                .apellido2(request.getApellido2())
                .email(request.getEmail())
                .clave(request.getClave())
                .telefono(request.getTelefono())
                .build();

        try {
            tecnicoService.registrarTecnico(nuevoTecnico);
        } catch (DataIntegrityViolationException ex) {
            String message = "El E-mail o el Teléfono ya está Registrado en la Base de Datos";
            String rawMessage = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
            if (rawMessage != null) {
                String normalized = rawMessage.toLowerCase();
                if (normalized.contains("email")) {
                    message = "El E-mail ya está Registrado en la Base de Datos";
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> actualizarRol(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequestDTO request,
            Authentication authentication
    ) {
        tecnicoService.actualizarRolUsuario(id, request.getRoleId(), authentication.getName());
        return ResponseEntity.ok("Rol actualizado exitosamente");
    }

    @PatchMapping("/me/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> actualizarMiClave(
            @Valid @RequestBody UpdateUserPasswordRequestDTO request,
            Authentication authentication
    ) {
        tecnicoService.actualizarMiClave(authentication.getName(), request.getNuevaClave());
        return ResponseEntity.ok("Clave actualizada exitosamente");
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> actualizarClaveUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserPasswordRequestDTO request
    ) {
        tecnicoService.actualizarClaveUsuario(id, request.getNuevaClave());
        return ResponseEntity.ok("Clave actualizada exitosamente");
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> actualizar(@PathVariable Long id, @RequestBody Tecnico request) {
        try {
            tecnicoService.actualizarTecnico(id, request);
            return ResponseEntity.ok("Técnico actualizado exitosamente en el sistema");
        } catch (DataIntegrityViolationException ex) {
            String message = "El E-mail o el Teléfono ya está Registrado en la Base de Datos";
            String rawMessage = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
            if (rawMessage != null) {
                String normalized = rawMessage.toLowerCase();
                if (normalized.contains("email")) {
                    message = "El E-mail ya está Registrado en la Base de Datos";
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
