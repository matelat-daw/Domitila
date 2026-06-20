package domitila.entity.converter;

import domitila.enums.Sexo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SexoConverter implements AttributeConverter<Sexo, String> {

    @Override
    public String convertToDatabaseColumn(Sexo attribute) {
        return attribute == null ? null : attribute.getDisplayName();
    }

    @Override
    public Sexo convertToEntityAttribute(String dbData) {
        return Sexo.fromValue(dbData);
    }
}
