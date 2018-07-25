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
package fr.cnes.regards.modules.models.domain.attributes.restriction.validator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * Exclude PL/SQL Keyword
 *
 * @author Christophe Mertz
 *
 */
public class CheckAttributeNameValidator implements ConstraintValidator<CheckAttributeName, String> {

    /**
     * File containing the unauthorized keywords for the attribute name
     */
    private final String FILE_SQL_KEYWORDS = "/SQL_keywords.properties";

    @Override
    public void initialize(CheckAttributeName constraintAnnotation) {
        // Nothing to do
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> list = new ArrayList<>();

        InputStream in = getClass().getResourceAsStream(FILE_SQL_KEYWORDS);
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(in));
        list = bufferReader.lines().filter(s -> s.equalsIgnoreCase(value)).collect(Collectors.toList());

        return list.size() == 0;
    }

}
