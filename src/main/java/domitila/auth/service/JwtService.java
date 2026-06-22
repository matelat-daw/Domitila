package domitila.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtService {

    // 1. Llave secreta inyectada desde application.properties (mínimo 256 bits / 32 caracteres)
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    // 2. Tiempo de expiración del access token en milisegundos
    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    @Value("${security.jwt.refresh-expiration-time}")
    private long refreshTokenExpiration;

    // Agrega estas dos variables arriba en tu clase JwtService
    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    @Value("${security.jwt.audience}")
    private String jwtAudience;

    private SecretKey signingKey;

    @PostConstruct
    void initializeSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

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
        final Claims claims = extractAllClaims(token);
        final String username = claims.getSubject();
        final String issuer = claims.getIssuer();

        return (username.equals(userDetails.getUsername())) 
            && (jwtIssuer.equals(issuer))
            && hasExpectedAudience(claims)
            && !claims.getExpiration().before(new Date());
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

    private boolean hasExpectedAudience(Claims claims) {
        Object audienceClaim = claims.get("aud");
        if (audienceClaim instanceof String audience) {
            return jwtAudience.equals(audience);
        }

        if (audienceClaim instanceof Collection<?> audiences) {
            return audiences.contains(jwtAudience);
        }

        return false;
    }

    private SecretKey getSignInKey() {
        return signingKey;
    }

    // 1. Generar el Refresh Token
    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    // 2. Validar si el token es un Refresh Token válido
    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // El método isTokenExpired(token) debe invocar internamente al Parser moderno de JJWT
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // 1. Verifica si el token ya expiró
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 2. Extrae la fecha de expiración específica del token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public long getAccessTokenExpiration() {
        return jwtExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
