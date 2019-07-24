/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.domain.models;

/**
 * Visitor for ICalculationModel plugins
 *
 * @author Sylvain Vissiere-Guerinet
 * @param <T>
 */
public interface IComputedAttributeVisitor<T> {

    /**
     * Visit method from the visitor design pattern
     * @param pPlugin {@link IComputedAttribute}<P, U>
     * @param <P> Type of entity on which the attribute will be added
     * @param <U> Type of the attribute value
     * @return T
     */
    <P, U> T visit(IComputedAttribute<P, U> pPlugin);

}
