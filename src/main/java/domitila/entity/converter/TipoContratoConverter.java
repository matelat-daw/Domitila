package domitila.entity.converter;

import domitila.enums.TipoContrato;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TipoContratoConverter implements AttributeConverter<TipoContrato, String> {

    @Override
    public String convertToDatabaseColumn(TipoContrato attribute) {
        return attribute == null ? null : attribute.getDisplayName();
    }

    @Override
    public TipoContrato convertToEntityAttribute(String dbData) {
        return TipoContrato.fromValue(dbData);
    }
}
