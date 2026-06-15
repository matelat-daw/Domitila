package domitila.controller;

import domitila.dto.AuthResponseDTO;
import domitila.dto.LoginRequestDTO;
import domitila.dto.RegisterRequestDTO;
import domitila.entity.Tecnico;
import domitila.service.JwtService;
import domitila.service.TecnicoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TecnicoService tecnicoService; // Inyectamos el servicio para el Registro

    // 1. ENDPOINT DE LOGIN (Ya lo tenías listo)
    @PostMapping("/login")
    public ResponseEntity<String> login(
        @Valid @RequestBody LoginRequestDTO request, 
        HttpServletResponse response // 👈 Inyectamos la respuesta HTTP nativa
    ) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getClave())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // 1. Generamos el token de forma normal
        String token = jwtService.generateToken(userDetails);

        // 2. Creamos la cookie con el nombre "jwt" y le asignamos el valor del token
        Cookie jwtCookie = new Cookie("jwt", token);
        
        // 3. Configuración de seguridad crítica de la cookie 🛡️
        jwtCookie.setHttpOnly(true);   // El frontend NO puede leerla con JavaScript. Protege contra XSS.
        jwtCookie.setSecure(false);    // Cambiar a 'true' en producción cuando usen HTTPS.
        jwtCookie.setPath("/");        // La cookie estará disponible para todas las rutas de la API.
        jwtCookie.setMaxAge(86400);    // Tiempo de vida en segundos (24 horas, a juego con tu JWT).

        // 4. Añadimos la cookie a la respuesta
        response.addCookie(jwtCookie);

        return ResponseEntity.ok("Login exitoso. Cookie establecida.");
    }

    // 2. ENDPOINT DE REGISTRO (Nuevo 🚀)
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO request) {
        // Transformamos el DTO en una entidad limpia de tipo Tecnico usando el Builder de Lombok
        Tecnico nuevoTecnico = Tecnico.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .clave(request.getClave()) // El servicio se encargará de encriptarla automáticamente
                .telefono(request.getTelefono())
                .build();

        tecnicoService.registrarTecnico(nuevoTecnico);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Técnico registrado exitosamente en el sistema");
    }

    // 3. ENDPOINT DE LOGOUT (Nuevo 🚀)
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        /* 
         * Nota técnica para el equipo: Al usar JWT (Stateless), el servidor no guarda sesiones.
         * El "Logout" real ocurre cuando el Frontend (Angular/React) borra el token de su memoria.
         * Dejamos este endpoint listo para confirmar la acción o por si luego implementan 
         * una base de datos en Redis para invalidar tokens (Token Blacklisting).
         */
        return ResponseEntity.ok("Sesión cerrada exitosamente. Recuerda eliminar el token en el cliente.");
    }
}