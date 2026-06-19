package domitila.security;

import domitila.entity.RoleName;
import domitila.entity.Personal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class PersonalDetails implements UserDetails {

    private final Personal personal;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<RoleName> roles = personal.getRoles();
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return personal.getClave();
    }

    @Override
    public String getUsername() {
        return personal.getCorreoElectronico();
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
}
