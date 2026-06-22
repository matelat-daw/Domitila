package domitila.auth.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum ConvenioLaboral {
    ACCION_SOCIAL("Accion social"),
    REFORMA_JUVENIL("Reforma juvenil");

    private final String displayName;

    ConvenioLaboral(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ConvenioLaboral fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = normalize(value);
        return Arrays.stream(values())
                .filter(convenio -> normalize(convenio.name()).equals(normalized)
                        || normalize(convenio.displayName).equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Convenio laboral inválido: " + value));
    }

    private static String normalize(String value) {
        return value.trim()
                .toUpperCase()
                .replace('Á', 'A')
                .replace('É', 'E')
                .replace('Í', 'I')
                .replace('Ó', 'O')
                .replace('Ú', 'U')
                .replace('Ü', 'U')
                .replaceAll("[\\s-]+", "_");
    }
}
