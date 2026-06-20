package domitila.controller;

import domitila.dto.RegisterRequestDTO;
import domitila.dto.UpdateProfileImageRequest;
import domitila.dto.UpdateUserPasswordRequestDTO;
import domitila.dto.UpdateUserRoleRequestDTO;
import domitila.dto.UserSummaryDTO;
import domitila.entity.Personal;
import domitila.service.PersonalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final PersonalService personalService;

    @GetMapping
    public ResponseEntity<Page<UserSummaryDTO>> obtenerUsuarios(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido1,
            @PageableDefault(size = 10, sort = {"nombre", "apellido1"}) Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(personalService.obtenerPersonalExcepto(authentication.getName(), nombre, apellido1, pageable));
    }

    @PostMapping
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO request) {
        Personal nuevoPersonal = Personal.builder()
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
                .imagenPerfil(request.imagenPerfil())
                .activo(request.activo())
                .diasVacaciones(request.diasVacaciones())
                .idCategoriaProfesional(request.idCategoriaProfesional())
                .build();

        personalService.registrarPersonal(nuevoPersonal);

        return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Personal registrado exitosamente en el sistema");
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<String> actualizarRol(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRoleRequestDTO request,
            Authentication authentication
    ) {
        personalService.actualizarRolPersonal(id, request.role(), request.action(), authentication.getName());
        return ResponseEntity.ok("Roles actualizados exitosamente");
    }

    @PatchMapping("/me/password")
    public ResponseEntity<String> actualizarMiClave(
            @Valid @RequestBody UpdateUserPasswordRequestDTO request,
            Authentication authentication
    ) {
        personalService.actualizarMiClave(authentication.getName(), request.nuevaClave());
        return ResponseEntity.ok("Clave actualizada exitosamente");
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<String> actualizarClaveUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserPasswordRequestDTO request
    ) {
        Integer idInt = id.intValue();
        personalService.actualizarClavePersonal(idInt, request.nuevaClave());
        return ResponseEntity.ok("Clave actualizada exitosamente");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> actualizar(@PathVariable Integer id, @RequestBody Personal request) {
        personalService.actualizarPersonal(id, request);
        return ResponseEntity.ok("Personal actualizado exitosamente en el sistema");
    }

    @PostMapping(value = "/{id}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> actualizarImagenPerfil(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file
    ) {
        String rutaImagen = personalService.actualizarImagenPerfil(id, file);
        return ResponseEntity.ok(rutaImagen);
    }

    @PatchMapping("/{id}/profile-image")
    public ResponseEntity<String> actualizarRutaImagenPerfil(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateProfileImageRequest request
    ) {
        String rutaImagen = personalService.actualizarRutaImagenPerfil(id, request.imagenPerfil());
        return ResponseEntity.ok(rutaImagen);
    }
}
