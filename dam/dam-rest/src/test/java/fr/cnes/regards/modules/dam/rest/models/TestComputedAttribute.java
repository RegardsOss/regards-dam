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
package fr.cnes.regards.modules.dam.rest.models;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.dam.domain.models.ComputationPlugin;
import fr.cnes.regards.modules.dam.domain.models.IComputedAttribute;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(description = "plugin there just for tests in model-rest", author = "REGARDS Team", contact = "regards@c-s.fr",
        license = "GPLv3", owner = "CSSI", url = "https://github.com/RegardsOss", id = "TestComputedAttribute",
        version = "1.0.0")
@ComputationPlugin(supportedType = AttributeType.STRING)
public class TestComputedAttribute implements IComputedAttribute<Long, String> {

    @Override
    public String getResult() {
        return null;
    }

    @Override
    public void compute(Long pPartialData) {

    }

    @Override
    public AttributeModel getAttributeToCompute() {
        return null;
    }

}
