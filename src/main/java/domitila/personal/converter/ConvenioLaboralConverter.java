package domitila.personal.converter;

import domitila.personal.ConvenioLaboral;
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
