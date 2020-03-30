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
package fr.cnes.regards.modules.model.domain.event;

import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.Fragment;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * {@link AttributeModel} event common information
 *
 * @author Marc Sordi
 *
 */
public abstract class AbstractAttributeModelEvent implements ISubscribable {

    /**
     * {@link Fragment} name
     */
    private String fragmentName;

    /**
     * {@link AttributeModel} name
     */
    private String attributeName;

    /**
     * {@link PropertyType}
     */
    private PropertyType PropertyType;

    public AbstractAttributeModelEvent() {
    }

    public AbstractAttributeModelEvent(AttributeModel pAttributeModel) {
        this.fragmentName = pAttributeModel.getFragment().getName();
        this.attributeName = pAttributeModel.getName();
        this.PropertyType = pAttributeModel.getType();
    }

    public PropertyType getPropertyType() {
        return PropertyType;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getFragmentName() {
        return fragmentName;
    }

    public void setFragmentName(String pFragmentName) {
        fragmentName = pFragmentName;
    }

    public void setAttributeName(String pAttributeName) {
        attributeName = pAttributeName;
    }

    public void setPropertyType(PropertyType pPropertyType) {
        PropertyType = pPropertyType;
    }

}