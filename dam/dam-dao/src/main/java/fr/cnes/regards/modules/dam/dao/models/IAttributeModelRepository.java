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
package fr.cnes.regards.modules.dam.dao.models;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;

/**
 *
 * {@link AttributeModel} repository
 *
 * @author Marc Sordi
 *
 */
@Repository
public interface IAttributeModelRepository
        extends CrudRepository<AttributeModel, Long>, JpaSpecificationExecutor<AttributeModel> {

    List<AttributeModel> findByType(AttributeType pType);

    List<AttributeModel> findByTypeAndFragmentName(AttributeType pType, String pFragmentName);

    List<AttributeModel> findByFragmentName(String pFragmentName);

    AttributeModel findByNameAndFragmentName(String pAttributeName, String pFragmentName);

    List<AttributeModel> findByFragmentId(Long pFragmentId);

    /**
     * Find attributes by name
     * @param fragmentName
     * @return attributes
     */
    Collection<AttributeModel> findByName(String fragmentName);
}