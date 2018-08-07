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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;

/**
 * {@link ModelAttrAssoc} repository
 *
 * @author Marc Sordi
 */
public interface IModelAttrAssocRepository extends JpaRepository<ModelAttrAssoc, Long> {

    List<ModelAttrAssoc> findByModelId(Long pModelId);

    List<ModelAttrAssoc> findByModelName(String modelName);

    ModelAttrAssoc findByModelIdAndAttribute(Long pModelId, AttributeModel pAttributeModel);

    /**
     * Find page attribute which are associated to at least one of the models
     * @param pModelIds
     * @param pPageable
     * @return a page of attribute which are associated to at least one of the models
     */
    @Query("SELECT assoc.attribute FROM ModelAttrAssoc assoc WHERE assoc.model.id IN :modelIds")
    Page<AttributeModel> findAllAttributeByModelIdIn(@Param("modelIds") Collection<Long> pModelIds, Pageable pPageable);

    /**
     * Find all the model attribute association which model is one of the given, represented by their ids
     * @param pModelsIds
     * @return the model attribute assocations
     */
    Collection<ModelAttrAssoc> findAllByModelIdIn(Collection<Long> pModelsIds);

    /**
     * Find all model attribute associations by attribute id
     * @param attrId
     * @return the model attribute associations
     */
    Collection<ModelAttrAssoc> findAllByAttributeId(Long attrId);
}
