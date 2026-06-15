package domitila.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tecnicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tecnico { // <-- Ya NO implementa UserDetails 🌟

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, length = 256)
    private String clave;

    private String telefono;
}