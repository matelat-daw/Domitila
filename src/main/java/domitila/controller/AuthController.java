package domitila.controller;

import domitila.dto.LoginRequestDTO;
import domitila.dto.RegisterRequestDTO;
import domitila.entity.Tecnico;
import domitila.service.JwtService;
import domitila.service.TecnicoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String JWT_COOKIE_NAME = "jwt";
    private static final Duration JWT_COOKIE_DURATION = Duration.ofDays(1);
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TecnicoService tecnicoService;

    // 1. ENDPOINT DE LOGIN (Ya lo tenías listo)
    @PostMapping("/login")
    public ResponseEntity<String> login(
        @Valid @RequestBody LoginRequestDTO request,
        HttpServletResponse response
    ) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getClave())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        response.addHeader(HttpHeaders.SET_COOKIE, buildJwtCookie(token, JWT_COOKIE_DURATION).toString());
        return ResponseEntity.ok("Login exitoso. Cookie establecida.");
    }

    // 2. ENDPOINT DE REGISTRO (Nuevo 🚀)
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO request) {
        Tecnico nuevoTecnico = Tecnico.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .clave(request.getClave())
                .telefono(request.getTelefono())
                .build();

        tecnicoService.registrarTecnico(nuevoTecnico);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Técnico registrado exitosamente en el sistema");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Set the secure flag to true for production security in logout cookie
        response.addHeader(HttpHeaders.SET_COOKIE, buildJwtCookie("", Duration.ZERO).toString());
        return ResponseEntity.ok("Sesión cerrada exitosamente. Recuerda eliminar el token en el cliente.");
    }

    private ResponseCookie buildJwtCookie(String token, Duration maxAge) {
        return ResponseCookie.from(JWT_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}