package domitila.personal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdatePersonalRequestDTO(
        @Size(min = 2, max = 32, message = "El nombre debe tener entre 2 y 32 caracteres")
        String nombre,

        @Size(max = 24, message = "El primer apellido no puede tener más de 24 caracteres")
        String apellido1,

        @Size(max = 24, message = "El segundo apellido no puede tener más de 24 caracteres")
        String apellido2,

        @Email(message = "El formato del correo electrónico no es válido")
        String correoElectronico,

        String telefono,

        @Size(max = 15, message = "El DNI no puede tener más de 15 caracteres")
        String dni,

        Integer generoId,
        LocalDate fechaNacimiento,

        @Size(max = 255, message = "El domicilio no puede tener más de 255 caracteres")
        String domicilioCompleto,

        Integer numeroHijos,
        Integer tipoJornadaId,
        BigDecimal horasJornadaParcial,
        Integer tipoContratoId,
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