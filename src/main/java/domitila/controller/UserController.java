package domitila.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import domitila.dto.RegisterRequestDTO;
import domitila.entity.Tecnico;
import domitila.service.TecnicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

        tecnicoService.registrarTecnico(nuevoTecnico);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Técnico registrado exitosamente en el sistema");
    }
}