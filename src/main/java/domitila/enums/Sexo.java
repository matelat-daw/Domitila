package domitila.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum Sexo {
    NO_BINARIO("No Binario"),
    MUJER("Mujer"),
    HOMBRE("Varón");

    private final String displayName;

    Sexo(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static Sexo fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = normalize(value);
        return Arrays.stream(values())
                .filter(sexo -> normalize(sexo.name()).equals(normalized) || normalize(sexo.displayName).equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sexo inválido: " + value));
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
