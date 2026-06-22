package domitila.auth.entity.converter;

import domitila.auth.enums.ConvenioLaboral;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ConvenioLaboralConverter implements AttributeConverter<ConvenioLaboral, String> {

    @Override
    public String convertToDatabaseColumn(ConvenioLaboral attribute) {
        return attribute == null ? null : attribute.getDisplayName();
    }

    @Override
    public ConvenioLaboral convertToEntityAttribute(String dbData) {
        return ConvenioLaboral.fromValue(dbData);
    }
}
