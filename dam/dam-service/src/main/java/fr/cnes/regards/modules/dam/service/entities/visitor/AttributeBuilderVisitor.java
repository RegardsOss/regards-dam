/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.service.entities.visitor;

import fr.cnes.regards.modules.model.domain.ComputationPlugin;
import fr.cnes.regards.modules.model.domain.IComputedAttribute;
import fr.cnes.regards.modules.model.domain.IComputedAttributeVisitor;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * Visitor handling the logic of creating an AbstractProperty according to the AttributeModel computed by the
 * ICalculationModel plugin
 *
 * @author Sylvain Vissiere-Guerinet
 */
public class AttributeBuilderVisitor implements IComputedAttributeVisitor<IProperty<?>> {

    @Override
    public <P, U> IProperty<?> visit(IComputedAttribute<P, U> plugin) {
        AttributeModel attr = plugin.getAttributeToCompute();
        ComputationPlugin computationPlugin = plugin.getClass().getAnnotation(ComputationPlugin.class);
        PropertyType attributeType = computationPlugin.supportedType();
        if (attr.getFragment().isDefaultFragment()) {
            return IProperty.forType(attributeType, attr.getName(), plugin.getResult());
        } else {
            return IProperty.buildObject(attr.getFragment().getName(),
                                         IProperty.forType(attributeType, attr.getName(), plugin.getResult()));
        }
    }

}
