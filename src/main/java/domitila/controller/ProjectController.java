package domitila.controller;

import domitila.dto.ProyectoDTO;
import domitila.service.ProyectoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
@PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
public class ProjectController {
    private final ProyectoService proyectoService;

    @GetMapping
    public List<ProyectoDTO> obtenerProyectos(Authentication authentication) {
        return proyectoService.obtenerProyectos(authentication);
    }

    @GetMapping("/{id}")
    public ProyectoDTO obtenerProyectoPorId(@PathVariable Long id, Authentication authentication) {
        return proyectoService.obtenerProyectoPorId(id, authentication);
    }

    @PostMapping("/{projectId}/tecnicos/{tecnicoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProyectoDTO> asignarTecnicoAProyecto(
            @PathVariable("projectId") Long projectId,
            @PathVariable("tecnicoId") Long tecnicoId
    ) {
        return ResponseEntity.ok(proyectoService.asignarTecnicoAProyecto(projectId, tecnicoId));
    }
}
