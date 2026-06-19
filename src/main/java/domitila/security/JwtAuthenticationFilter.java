package domitila.security;

import domitila.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils; // 👈 Importante para simplificar cookies

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String JWT_COOKIE_NAME = "jwt";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Solo omite endpoints públicos de autenticación. `/api/auth/me` sí necesita JWT.
        String uri = request.getRequestURI();
        return "/api/auth/login".equals(uri)
                || "/api/auth/refresh".equals(uri)
                || "/api/auth/logout".equals(uri);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Extraer la cookie usando la utilidad nativa de Spring
        Cookie jwtCookie = WebUtils.getCookie(request, JWT_COOKIE_NAME);
        
        if (jwtCookie == null || jwtCookie.getValue() == null || jwtCookie.getValue().isBlank() 
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = jwtCookie.getValue();

        try {
            // 2. Extraer el nombre de usuario
            String userEmail = jwtService.extractUsername(jwt);
            
            // 3. Optimización: Validar que el token no esté expirado ANTES de ir a la Base de Datos
            if (userEmail != null && !jwtService.isTokenExpired(jwt)) { // 👈 Cambiado por eficiencia
                
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            // Opcional: Podrías interceptar aquí para enviar un 401 explícito si lo deseas
        }

        filterChain.doFilter(request, response);
    }
}
