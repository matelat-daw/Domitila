package domitila.dto;

import domitila.enums.ConvenioLaboral;
import domitila.enums.GrupoProfesional;
import domitila.enums.Sexo;
import domitila.enums.TipoContrato;
import domitila.enums.TipoJornada;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterRequestDTO (

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre must tener entre 2 y 100 caracteres")
    String nombre,

    @NotBlank(message = "El primer apellido no puede estar vacío")
    @Size(max = 24, message = "El primer apellido no puede tener más de 24 caracteres")
    String apellido1,

    @Size(max = 24, message = "El segundo apellido no puede tener más de 24 caracteres")
    String apellido2,

    @NotBlank(message = "El correo electrónico no puede estar vacío")
    @Email(message = "El formato del correo electrónico no es válido")
    String correoElectronico,

    @NotBlank(message = "La clave no puede estar vacía")
    @Size(min = 6, message = "La clave debe tener al menos 6 caracteres")
    String clave,

    String telefono,

    @NotBlank(message = "El DNI es obligatorio")
    @Size(max = 15, message = "El DNI no puede tener más de 15 caracteres")
    String dni,

    Sexo sexo,

    LocalDate fechaNacimiento,

    @Size(max = 255, message = "El domicilio no puede tener más de 255 caracteres")
    String domicilioCompleto,

    Integer numeroHijos,

    TipoJornada tipoJornada,

    BigDecimal horasJornadaParcial,

    TipoContrato tipoContrato,

    GrupoProfesional grupoProfesional,

    ConvenioLaboral convenioLaboral,

    @Size(max = 34, message = "El número de cuenta no puede tener más de 34 caracteres")
    String numeroCuenta,

    Boolean discapacidad,

    LocalDate fechaAlta,

    LocalDate fechaBaja,

    BigDecimal salarioBruto,

    @Size(max = 150, message = "La titulación no puede tener más de 150 caracteres")
    String titulacion,

    Boolean vehiculo,

    @Size(max = 255, message = "La ruta de la imagen de perfil no puede tener más de 255 caracteres")
    String imagenPerfil,

    Boolean activo,

    Integer diasVacaciones,

    Integer idCategoriaProfesional
) {}
