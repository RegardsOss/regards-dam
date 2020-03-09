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
package fr.cnes.regards.modules.dam.service.models.event;

import org.springframework.context.ApplicationEvent;

import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;

/**
 * Event sent when a plugin configuration is updated on a computed attribute
 * @author oroussel
 */
@SuppressWarnings("serial")
public class ComputedAttributeModelEvent extends ApplicationEvent {

    public ComputedAttributeModelEvent(ModelAttrAssoc source) {
        super(source);
    }

    @Override
    public ModelAttrAssoc getSource() {
        return (ModelAttrAssoc) super.getSource();
    }
}
