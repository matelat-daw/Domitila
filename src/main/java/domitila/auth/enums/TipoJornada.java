package domitila.auth.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum TipoJornada {
    COMPLETA("Completa"),
    PARCIAL("Parcial");

    private final String displayName;

    TipoJornada(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static TipoJornada fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = normalize(value);
        return Arrays.stream(values())
                .filter(tipo -> normalize(tipo.name()).equals(normalized) || normalize(tipo.displayName).equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de jornada inválido: " + value));
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
