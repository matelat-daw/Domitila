package domitila.auth.config;

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        FieldError firstFieldError = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);

        if (firstFieldError != null && firstFieldError.getDefaultMessage() != null) {
            return ResponseEntity.badRequest().body(firstFieldError.getDefaultMessage());
        }

        String message = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Datos de entrada no válidos")
                .collect(Collectors.joining(". "));

        return ResponseEntity.badRequest().body(message.isBlank() ? "Datos de entrada no válidos" : message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable mostSpecificCause = ex.getMostSpecificCause();
        String message = mostSpecificCause != null ? mostSpecificCause.getMessage() : ex.getMessage();
        String normalized = message == null ? "" : message.toLowerCase();

        if (normalized.contains("localdate")) {
            return ResponseEntity.badRequest().body("Hay una fecha con formato inválido. Usa el formato AAAA-MM-DD.");
        }
        if (normalized.contains("integer")) {
            return ResponseEntity.badRequest().body("Hay un campo numérico entero con un valor inválido.");
        }
        if (normalized.contains("bigdecimal") || normalized.contains("double") || normalized.contains("float")) {
            return ResponseEntity.badRequest().body("Hay un campo numérico con un valor inválido.");
        }
        if (normalized.contains("generoid") || normalized.contains("genero")) {
            return ResponseEntity.badRequest().body("El identificador del género no es válido.");
        }
        if (normalized.contains("tipojornadaid") || normalized.contains("tipo_jornada")) {
            return ResponseEntity.badRequest().body("El identificador del tipo de jornada no es válido.");
        }
        if (normalized.contains("tipocontratoid") || normalized.contains("tipo_contrato")) {
            return ResponseEntity.badRequest().body("El identificador del tipo de contrato no es válido.");
        }
        if (normalized.contains("grupoprofesional") || normalized.contains("grupo_profesional")) {
            return ResponseEntity.badRequest().body("El grupo profesional es inválido. Usa 1, 2, 3 o 4.");
        }
        if (normalized.contains("conveniolaboral") || normalized.contains("convenio_laboral")) {
            return ResponseEntity.badRequest().body("El convenio laboral es inválido. Usa Accion social o Reforma juvenil.");
        }

        return ResponseEntity.badRequest().body("La solicitud contiene datos con formato incorrecto.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body("El parámetro " + ex.getName() + " no tiene un formato válido.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body("Falta el parámetro obligatorio: " + ex.getParameterName() + ".");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<String> handleMissingServletRequestPart(MissingServletRequestPartException ex) {
        if ("file".equalsIgnoreCase(ex.getRequestPartName())) {
            return ResponseEntity.badRequest().body("Debes adjuntar una imagen en el campo file.");
        }
        return ResponseEntity.badRequest().body("Falta una parte obligatoria de la solicitud: " + ex.getRequestPartName() + ".");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .filter(msg -> msg != null && !msg.isBlank())
                .collect(Collectors.joining(". "));

        return ResponseEntity.badRequest().body(message.isBlank() ? "Datos de entrada no válidos." : message);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        String body = ex.getReason() != null ? ex.getReason() : "Ha ocurrido un error en la solicitud.";
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String rawMessage = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String message = resolveConflictMessage(rawMessage);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest().body("La imagen supera el tamaño máximo permitido de 5 MB.");
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleMultipartException(MultipartException ex) {
        return ResponseEntity.badRequest().body("La solicitud multipart no es válida. Revisa el archivo enviado.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body("El tipo de contenido no es compatible con este endpoint.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        String message = ex.getMessage();
        return ResponseEntity.badRequest()
                .body(message == null || message.isBlank() ? "La solicitud contiene valores no válidos." : message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permisos para realizar esta operación.");
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<String> handleAuthenticationCredentialsNotFound(AuthenticationCredentialsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("No se encontraron credenciales de autenticación.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("La autenticación ha fallado.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpectedException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ha ocurrido un error interno del servidor.");
    }

    private String resolveConflictMessage(String rawMessage) {
        if (rawMessage != null) {
            String normalized = rawMessage.toLowerCase();
            if (normalized.contains("dni")) {
                return "El DNI ya está registrado en la base de datos";
            }
            if (normalized.contains("correo_electronico") || normalized.contains("correo electronico") || normalized.contains("email")) {
                return "El correo electrónico ya está registrado en la base de datos";
            }
            if (normalized.contains("telefono") || normalized.contains("teléfono")) {
                return "El teléfono ya está registrado en la base de datos";
            }
        }

        return "Ya existe un registro con datos únicos repetidos.";
    }
}
