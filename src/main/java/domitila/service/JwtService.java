package domitila.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    // 1. Llave secreta inyectada desde application.properties (mínimo 256 bits / 32 caracteres)
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    // 2. Tiempo de expiración inyectado (ej: 86400000 para 24 horas)
    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    // Agrega estas dos variables arriba en tu clase JwtService
    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    @Value("${security.jwt.audience}")
    private String jwtAudience;

    // GENERAR TOKEN (Para el Login)
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Modifica tu método existente para que incluya los nuevos parámetros
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // El email del usuario
                .issuer(jwtIssuer)                  // <-- EMISOR GENÉRICO AGREGADO
                .audience().add(jwtAudience).and()  // <-- AUDIENCIA AGREGADA (API 0.12.x)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey()) 
                .compact();
    }

    // OBTENER EL USERNAME/EMAIL DEL TOKEN
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // VALIDAR SI EL TOKEN ES CORRECTO Y PERTENECE AL USUARIO
    public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    final String issuer = extractClaim(token, Claims::getIssuer);
    
        return (username.equals(userDetails.getUsername())) 
            && (jwtIssuer.equals(issuer)) // Valida que el emisor sea el tuyo
            && !isTokenExpired(token);
    }

    // Métodos auxiliares privados de JJWT
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}