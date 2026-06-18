package domitila.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import domitila.dto.RegisterRequestDTO;
import domitila.entity.Tecnico;
import domitila.service.TecnicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final TecnicoService tecnicoService;

    // 2. ENDPOINT DE REGISTRO (Nuevo 🚀)
    @PostMapping("/register")
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
