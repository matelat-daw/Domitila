package domitila.security;

import domitila.entity.Tecnico;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class TecnicoDetails implements UserDetails {

    private final Tecnico tecnico;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(normalizeRoleName(tecnico.getRole().getName())));
    }

    @Override
    public String getPassword() {
        return tecnico.getClave();
    }

    @Override
    public String getUsername() {
        return tecnico.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private String normalizeRoleName(String roleName) {
        String normalizedRole = roleName.trim().toUpperCase(Locale.ROOT);
        return normalizedRole.startsWith("ROLE_") ? normalizedRole : "ROLE_" + normalizedRole;
    }
}