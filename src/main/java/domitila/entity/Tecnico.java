package domitila.entity;

import jakarta.persistence.*;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tecnico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "clave")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 32)
    private String nombre;

    @Column(nullable = false, length = 24)
    private String apellido1;

    @Column(length = 24)
    private String apellido2;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, length = 256)
    private String clave;

    @Column(nullable = false, unique = true)
    private String telefono;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tecnico_role",
            joinColumns = @JoinColumn(name = "tecnico_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}