package domitila.personal.converter;

import domitila.personal.GrupoProfesional;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class GrupoProfesionalConverter implements AttributeConverter<GrupoProfesional, String> {

    @Override
    public String convertToDatabaseColumn(GrupoProfesional attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public GrupoProfesional convertToEntityAttribute(String dbData) {
        return GrupoProfesional.fromValue(dbData);
    }
}
