package domitila.auth.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum GrupoProfesional {
    GRUPO_1("1"),
    GRUPO_2("2"),
    GRUPO_3("3"),
    GRUPO_4("4");

    private final String code;

    GrupoProfesional(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static GrupoProfesional fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(grupo -> grupo.code.equals(normalized) || grupo.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Grupo profesional inválido: " + value));
    }
}
