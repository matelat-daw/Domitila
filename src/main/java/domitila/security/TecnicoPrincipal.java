package domitila.security;

import domitila.entity.Tecnico;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class TecnicoPrincipal implements UserDetails {

    // Recibe la entidad original de forma interna
    private final Tecnico tecnico; 

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mantenemos el rol por defecto para los técnicos
        return List.of(new SimpleGrantedAuthority("ROLE_TECNICO"));
    }

    @Override
    public String getPassword() {
        return tecnico.getClave(); // Mapea tu campo 'clave'
    }

    @Override
    public String getUsername() {
        return tecnico.getEmail(); // Mapea tu campo 'email' como identificador
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}