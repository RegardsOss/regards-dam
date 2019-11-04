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
package fr.cnes.regards.modules.model.dao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;

/**
 * {@link ModelAttrAssoc} repository
 *
 * @author Marc Sordi
 */
public interface IModelAttrAssocRepository extends JpaRepository<ModelAttrAssoc, Long> {

    List<ModelAttrAssoc> findByModelId(Long pModelId);

    @EntityGraph(attributePaths = { "attribute.properties" })
    List<ModelAttrAssoc> findByModelName(String modelName);

    @EntityGraph(attributePaths = { "attribute.properties" })
    ModelAttrAssoc findByModelIdAndAttribute(Long pModelId, AttributeModel pAttributeModel);

    @Override
    @EntityGraph(attributePaths = { "attribute.properties" })
    List<ModelAttrAssoc> findAll();

    @Override
    @EntityGraph(attributePaths = { "attribute.properties" })
    Optional<ModelAttrAssoc> findById(Long id);

    /**
     * Find page attribute which are associated to at least one of the models
     * @param pModelIds
     * @param pPageable
     * @return a page of attribute which are associated to at least one of the models
     */
    @EntityGraph(attributePaths = { "attribute.properties" })
    Page<ModelAttrAssoc> findAllByModelIdIn(Collection<Long> pModelIds, Pageable pPageable);

    /**
     * Find all the model attribute association which model is one of the given, represented by their ids
     * @param pModelsIds
     * @return the model attribute assocations
     */
    @EntityGraph(attributePaths = { "attribute.properties" })
    Collection<ModelAttrAssoc> findAllByModelIdIn(Collection<Long> pModelsIds);

    /**
     * Find all model attribute associations by attribute id
     * @param attrId
     * @return the model attribute associations
     */
    @EntityGraph(attributePaths = { "attribute.properties" })
    Collection<ModelAttrAssoc> findAllByAttributeId(Long attrId);
}
