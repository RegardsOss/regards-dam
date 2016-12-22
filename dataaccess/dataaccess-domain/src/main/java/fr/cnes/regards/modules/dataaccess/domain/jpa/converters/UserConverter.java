/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.jpa.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Converter(autoApply = true)
public class UserConverter implements AttributeConverter<User, String> {

    @Override
    public String convertToDatabaseColumn(User pAttribute) {
        return pAttribute.getEmail();
    }

    @Override
    public User convertToEntityAttribute(String pDbData) {
        return new User(pDbData);
    }

}
