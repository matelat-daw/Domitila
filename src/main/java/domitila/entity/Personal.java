package domitila.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "personal_laboral")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"clave"})
// @ToString(exclude = {"clave", "roles", "proyectos"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Personal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trabajador")
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 32)
    private String nombre;

    @Column(name = "apellido1", nullable = false, length = 24)
    private String apellido1;

    @Column(name = "apellido2", length = 24)
    private String apellido2;

    @Column(name = "dni", nullable = false, length = 15, unique = true)
    @Builder.Default
    private String dni = "PENDIENTE";

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false, length = 16)
    @Builder.Default
    private Sexo sexo = Sexo.NO_BINARIO;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "correo_electronico", unique = true, nullable = false, length = 64)
    private String correoElectronico;

    @Column(name = "clave", nullable = false, length = 256)
    private String clave;

    @Column(name = "telefono", unique = true, length = 255)
    private String telefono;

    @Column(name = "domicilio_completo", length = 255)
    private String domicilioCompleto;

    @Column(name = "numero_hijos", nullable = false)
    @Builder.Default
    private Integer numeroHijos = 0;

    @Column(name = "tipo_jornada", nullable = false, length = 16)
    @Builder.Default
    private String tipoJornada = "Completa";

    @Column(name = "horas_jornada_parcial", precision = 10, scale = 2)
    private BigDecimal horasJornadaParcial;

    @Column(name = "tipo_contrato", nullable = false, length = 16)
    @Builder.Default
    private String tipoContrato = "Temporal";

    @Column(name = "grupo_profesional", nullable = false, length = 8)
    @Builder.Default
    private String grupoProfesional = "1";

    @Column(name = "convenio_laboral", length = 32)
    private String convenioLaboral;

    @Column(name = "numero_cuenta", length = 34)
    private String numeroCuenta;

    @Column(name = "discapacidad", nullable = false)
    @Builder.Default
    private Boolean discapacidad = false;

    @Column(name = "fecha_alta")
    private LocalDate fechaAlta;

    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    @Column(name = "salario_bruto", precision = 10, scale = 2)
    private BigDecimal salarioBruto;

    @Column(name = "titulacion", length = 150)
    private String titulacion;

    @Column(name = "vehiculo", nullable = false)
    @Builder.Default
    private Boolean vehiculo = false;

    @Column(name = "imagen_perfil", length = 255)
    private String imagenPerfil;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "dias_vacaciones")
    private Integer diasVacaciones;

    @Column(name = "id_categoria_profesional", nullable = false)
    @Builder.Default
    private Integer idCategoriaProfesional = 1;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "personal_laboral_role",
            joinColumns = @JoinColumn(name = "trabajador_id", referencedColumnName = "id_trabajador")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role_id", nullable = false, length = 20)
    @Builder.Default
    private Set<RoleName> roles = new HashSet<>();

    public RoleName getRole() {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.iterator().next();
    }

    // La tabla intermedia persiste una colección de roles, pero la lógica actual de la app
    // trabaja con un único rol por usuario desde el servicio y la seguridad.
    public void setRole(RoleName role) {
        if (role == null) {
            this.roles = new HashSet<>();
            return;
        }
        this.roles = new HashSet<>(Set.of(role));
    }

    public void addRole(RoleName role) {
        if (role == null) {
            return;
        }
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    public void removeRole(RoleName role) {
        if (role == null || this.roles == null) {
            return;
        }
        this.roles.remove(role);
    }

//     @ManyToMany(fetch = FetchType.LAZY)
//     @JoinTable(
//             name = "personal_laboral_proyecto",
//             joinColumns = @JoinColumn(name = "trabajador_id", referencedColumnName = "id_trabajador"),
//             inverseJoinColumns = @JoinColumn(name = "proyecto_id", referencedColumnName = "id_proyecto")
//     )
    // @Builder.Default
    // private Set<Proyecto> proyectos = new HashSet<>();
}
