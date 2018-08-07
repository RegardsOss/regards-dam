/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.dam.domain.dataaccess.jpa.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.User;

/**
 * User database converter
 *
 * @author Sylvain Vissiere-Guerinet
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
