/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.converter;

import java.util.Collections;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.springframework.http.MediaType;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Converter(autoApply = true)
public class MediaTypeConverter implements AttributeConverter<MediaType, String> {

    @Override
    public String convertToDatabaseColumn(MediaType pAttribute) {
        if (pAttribute == null) {
            return null;
        }
        return MediaType.toString(Collections.singletonList(pAttribute));
    }

    @Override
    public MediaType convertToEntityAttribute(String pDbData) {
        if (pDbData == null) {
            return null;
        }
        return MediaType.valueOf(pDbData);
    }

}
