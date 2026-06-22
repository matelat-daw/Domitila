package domitila.auth.entity.converter;

import domitila.auth.enums.TipoJornada;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TipoJornadaConverter implements AttributeConverter<TipoJornada, String> {

    @Override
    public String convertToDatabaseColumn(TipoJornada attribute) {
        return attribute == null ? null : attribute.getDisplayName();
    }

    @Override
    public TipoJornada convertToEntityAttribute(String dbData) {
        return TipoJornada.fromValue(dbData);
    }
}
