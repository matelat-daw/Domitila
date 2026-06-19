package domitila.service;

import domitila.dto.ProyectoDTO;
import domitila.dto.ProyectoTecnicoDTO;
import domitila.entity.Proyecto;
import domitila.entity.Role;
import domitila.entity.Tecnico;
import domitila.repository.ProyectoRepository;
import domitila.repository.TecnicoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProyectoService {
    private final ProyectoRepository proyectoRepository;
    private final TecnicoRepository tecnicoRepository;

    public List<ProyectoDTO> obtenerProyectos(Authentication authentication) {
        List<Proyecto> proyectos = esAdmin(authentication)
                ? proyectoRepository.findAllConTecnicos()
                : proyectoRepository.findAsignadosPorTecnicoEmail(authentication.getName());

        return proyectos.stream()
                .map(this::toProyectoDTO)
                .toList();
    }

    public ProyectoDTO obtenerProyectoPorId(Long id, Authentication authentication) {
        Proyecto proyecto = esAdmin(authentication)
                ? proyectoRepository.findByIdConTecnicos(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Proyecto no encontrado con ID: " + id
                        ))
                : proyectoRepository.findByIdAndTecnicoEmail(id, authentication.getName())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Proyecto no encontrado con ID: " + id
                        ));

        return toProyectoDTO(proyecto);
    }

    @Transactional
    public ProyectoDTO asignarTecnicoAProyecto(Long proyectoId, Long tecnicoId) {
        Proyecto proyecto = proyectoRepository.findByIdConTecnicos(proyectoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Proyecto no encontrado con ID: " + proyectoId
                ));

        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Técnico no encontrado con ID: " + tecnicoId
                ));

        if (!esTecnico(tecnico)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se pueden asignar usuarios con rol TÉCNICO a un proyecto"
            );
        }

        if (!tecnico.getProyectos().contains(proyecto)) {
            tecnico.getProyectos().add(proyecto);
        }
        if (!proyecto.getTecnicos().contains(tecnico)) {
            proyecto.getTecnicos().add(tecnico);
        }
        tecnicoRepository.save(tecnico);

        return toProyectoDTO(proyecto);
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equalsIgnoreCase(authority.getAuthority()));
    }

    private boolean esTecnico(Tecnico tecnico) {
        return tecnico.getRoles() != null
                && tecnico.getRoles().stream()
                        .map(Role::getName)
                        .anyMatch(role -> "TECNICO".equalsIgnoreCase(role) || "ROLE_TECNICO".equalsIgnoreCase(role));
    }

    private ProyectoDTO toProyectoDTO(Proyecto proyecto) {
        return new ProyectoDTO(
                proyecto.getId(),
                proyecto.getNombre(),
                proyecto.getDescripcion(),
                proyecto.getFechaInicio(),
                proyecto.getTecnicos() == null
                        ? List.of()
                        : proyecto.getTecnicos().stream()
                                .map(tecnico -> new ProyectoTecnicoDTO(
                                        tecnico.getId(),
                                        tecnico.getNombre(),
                                        tecnico.getApellido1(),
                                        tecnico.getApellido2(),
                                        tecnico.getEmail()
                                ))
                                .sorted(java.util.Comparator.comparing(ProyectoTecnicoDTO::nombre, String.CASE_INSENSITIVE_ORDER))
                                .toList()
        );
    }
}
