package domitila.controller;

import domitila.dto.LoginRequestDTO;
import domitila.dto.UserSummaryDTO;
import domitila.service.JwtService;
import domitila.service.PersonalService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String JWT_COOKIE_NAME = "jwt";
    private static final String REFRESH_COOKIE_NAME = "refresh_jwt";

    @Value("${security.jwt.cookie-secure:false}")
    private boolean secureCookie;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final PersonalService personalService;

    // 1. ENDPOINT DE LOGIN (Actualizado con Refresh Token y Estilo Moderno)
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correoElectronico(), request.clave())
            );
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            if (!canLogin(userDetails)) {
                // Si no puede loguearse, borramos cualquier cookie previa de inmediato
                ResponseCookie deleteAccessCookie = buildCookie(JWT_COOKIE_NAME, "", Duration.ZERO);
                ResponseCookie deleteRefreshCookie = buildCookie(REFRESH_COOKIE_NAME, "", Duration.ZERO);

                String errorMsg = (hasAuthority(userDetails, "ROLE_USUARIO") || hasAuthority(userDetails, "ROLE_USER"))
                        ? "Los usuarios con rol USUARIO no tienen permiso para loguearse."
                        : "No tienes permiso para loguearte. Si piensas que es un error, contacta con la Administradora.";

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString())
                        .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString())
                        .body(errorMsg);
            }

            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            ResponseCookie accessTokenCookie = buildCookie(
                    JWT_COOKIE_NAME,
                    accessToken,
                    Duration.ofMillis(jwtService.getAccessTokenExpiration())
            );
            ResponseCookie refreshTokenCookie = buildCookie(
                    REFRESH_COOKIE_NAME,
                    refreshToken,
                    Duration.ofMillis(jwtService.getRefreshTokenExpiration())
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body("Login exitoso. Cookies establecidas.");

        } catch (AuthenticationCredentialsNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales invalidas.");
        } catch (DisabledException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("La cuenta está desactivada.");
        } catch (org.springframework.security.core.AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Correo electrónico o clave incorrectos.");
        }
    }

    // 2. ENDPOINT DE REFRESH TOKEN
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(HttpServletRequest request) {
        Cookie refreshCookie = WebUtils.getCookie(request, REFRESH_COOKIE_NAME);

        if (refreshCookie == null || refreshCookie.getValue() == null || refreshCookie.getValue().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token ausente");
        }

        String refreshToken = refreshCookie.getValue();
        UserDetails userDetails = validateRefreshToken(refreshToken);
        String newAccessToken = jwtService.generateToken(userDetails);
        ResponseCookie accessTokenCookie = buildCookie(
                JWT_COOKIE_NAME,
                newAccessToken,
                Duration.ofMillis(jwtService.getAccessTokenExpiration())
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .body("Sesión extendida exitosamente");
    }

    // 3. ENDPOINT DE LOGOUT (Actualizado para limpiar ambas cookies)
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        ResponseCookie deleteAccessCookie = buildCookie(JWT_COOKIE_NAME, "", Duration.ZERO);
        ResponseCookie deleteRefreshCookie = buildCookie(REFRESH_COOKIE_NAME, "", Duration.ZERO);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString()) // 👈 Destruye ambas cookies
                .body("Sesión cerrada exitosamente.");
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSummaryDTO> me(Authentication authentication) {
        return ResponseEntity.ok(personalService.obtenerResumenPersonal(authentication.getName()));
    }

    // Método privado auxiliar unificado para construir cookies limpiamente
    private ResponseCookie buildCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    private boolean canLogin(UserDetails userDetails) {
        return hasAuthority(userDetails, "ROLE_ADMIN") || hasAuthority(userDetails, "ROLE_TECNICO");
    }

    private String buildLoginDeniedMessage(UserDetails userDetails) {
        return (hasAuthority(userDetails, "ROLE_USUARIO") || hasAuthority(userDetails, "ROLE_USER"))
                ? "Los usuarios con rol USUARIO no tienen permiso para loguearse."
                : "No tienes permiso para loguearte. Si piensas que es un error, contacta con la Administradora.";
    }

    private boolean hasAuthority(UserDetails userDetails, String authority) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> authority.equalsIgnoreCase(a.getAuthority()));
    }

    private UserDetails validateRefreshToken(String refreshToken) {
        try {
            String username = jwtService.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!userDetails.isEnabled()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está desactivada.");
            }
            if (!canLogin(userDetails)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, buildLoginDeniedMessage(userDetails));
            }

            if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token inválido o expirado");
            }

            return userDetails;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token inválido o expirado");
        }
    }
}
