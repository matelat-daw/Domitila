package domitila.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum TipoContrato {
    INDEFINIDO("Indefinido"),
    TEMPORAL("Temporal");

    private final String displayName;

    TipoContrato(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static TipoContrato fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = normalize(value);
        return Arrays.stream(values())
                .filter(tipo -> normalize(tipo.name()).equals(normalized) || normalize(tipo.displayName).equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de contrato inválido: " + value));
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
