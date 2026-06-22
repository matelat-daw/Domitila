package domitila.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import domitila.proyecto.Proyecto;
import domitila.auth.entity.converter.ConvenioLaboralConverter;
import domitila.auth.entity.converter.GrupoProfesionalConverter;
import domitila.auth.entity.converter.GeneroConverter;
import domitila.auth.entity.converter.TipoContratoConverter;
import domitila.auth.entity.converter.TipoJornadaConverter;
import domitila.auth.enums.ConvenioLaboral;
import domitila.auth.enums.GrupoProfesional;
import domitila.auth.enums.RoleName;
import domitila.auth.enums.Genero;
import domitila.auth.enums.TipoContrato;
import domitila.auth.enums.TipoJornada;
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
@ToString(exclude = {"clave", "roles", "proyectos"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Personal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
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

    @Convert(converter = GeneroConverter.class)
    @Column(name = "genero", nullable = false, length = 16)
    @Builder.Default
    private Genero genero = Genero.NO_BINARIO;

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

    @Convert(converter = TipoJornadaConverter.class)
    @Column(name = "tipo_jornada", nullable = false, length = 16)
    @Builder.Default
    private TipoJornada tipoJornada = TipoJornada.COMPLETA;

    @Column(name = "horas_jornada_parcial", precision = 10, scale = 2)
    private BigDecimal horasJornadaParcial;

    @Convert(converter = TipoContratoConverter.class)
    @Column(name = "tipo_contrato", nullable = false, length = 16)
    @Builder.Default
    private TipoContrato tipoContrato = TipoContrato.TEMPORAL;

    @Convert(converter = GrupoProfesionalConverter.class)
    @Column(name = "grupo_profesional", nullable = false, length = 8)
    @Builder.Default
    private GrupoProfesional grupoProfesional = GrupoProfesional.GRUPO_1;

    @Convert(converter = ConvenioLaboralConverter.class)
    @Column(name = "convenio_laboral", length = 32)
    private ConvenioLaboral convenioLaboral;

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
            joinColumns = @JoinColumn(name = "personal_laboral_id", referencedColumnName = "id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role_id", nullable = false, length = 20)
    @Builder.Default
    private Set<RoleName> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "personal_laboral_proyecto",
            joinColumns = @JoinColumn(name = "id_personal_laboral", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "id_proyecto", referencedColumnName = "id")
    )
    @Builder.Default
    @JsonIgnore
    private Set<Proyecto> proyectos = new HashSet<>();
}