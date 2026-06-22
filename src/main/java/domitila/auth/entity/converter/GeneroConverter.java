package domitila.auth.entity.converter;

import domitila.auth.enums.Genero;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class GeneroConverter implements AttributeConverter<Genero, String> {

    @Override
    public String convertToDatabaseColumn(Genero attribute) {
        return attribute == null ? null : attribute.getDisplayName();
    }

    @Override
    public Genero convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Genero.fromValue(dbData);
    }
}