/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.models.domain;

/**
 * Computation mode to set in model for an attribute.<br/>
 * Default to {@link ComputationMode#GIVEN}. {@link ComputationMode#COMPUTED} is only available for dataset model
 * attributes.
 *
 * @author Marc Sordi
 */
public enum ComputationMode {

    /**
     * Value must be given explicitly to create the entity / no computation task is applied!
     */
    GIVEN,

    /**
     * Value is computed according to a custom computation plugin.
     */
    COMPUTED;
}
