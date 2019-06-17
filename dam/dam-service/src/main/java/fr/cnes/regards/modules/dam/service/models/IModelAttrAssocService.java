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
package fr.cnes.regards.modules.dam.service.models;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.TypeMetadataConfMapping;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.Fragment;
import fr.cnes.regards.modules.dam.service.models.event.NewFragmentAttributeEvent;

/**
 * Model attribute association service description
 *
 * @author Marc Sordi
 * @author oroussel
 */
public interface IModelAttrAssocService extends ApplicationListener<NewFragmentAttributeEvent> {

    List<ModelAttrAssoc> getModelAttrAssocs(String modelName);

    ModelAttrAssoc bindAttributeToModel(String modelName, ModelAttrAssoc pModelAttribute) throws ModuleException;

    ModelAttrAssoc getModelAttrAssoc(String modelName, Long pAttributeId) throws ModuleException;

    ModelAttrAssoc getModelAttrAssoc(Long pModelId, AttributeModel pAttribute);

    ModelAttrAssoc updateModelAttribute(String modelName, Long pAttributeId, ModelAttrAssoc pModelAttribute)
            throws ModuleException;

    void unbindAttributeFromModel(String modelName, Long pAttributeId) throws ModuleException;

    List<ModelAttrAssoc> bindNSAttributeToModel(String modelName, Fragment pFragment) throws ModuleException;

    /**
     * Propagate a fragment update
     * @param added  {@link AttributeModel}
     */
    void updateNSBind(AttributeModel added);

    void unbindNSAttributeToModel(String modelName, Long pFragmentId);

    /**
     * Find all model attribute associations by attribute
     * @param attr
     * @return the model attribute associations
     */
    Collection<ModelAttrAssoc> retrieveModelAttrAssocsByAttributeId(AttributeModel attr);

    Model duplicateModelAttrAssocs(String sourceModelName, Model pTargetModel) throws ModuleException;

    void addAllModelAttributes(List<ModelAttrAssoc> modelAtts) throws ModuleException;

    /**
     * Retrieve the computed attributes association to a model, represented by its id
     * @param pId
     * @return computed attributes association to the model
     */
    Set<ModelAttrAssoc> getComputedAttributes(Long pId);

    /**
     * Find page attribute which are associated to at least one of the models
     * @param pModelNames
     * @param pPageable
     * @return a page of attribute which are associated to at least one of the models
     * @throws ModuleException
     */
    Page<AttributeModel> getAttributeModelsByName(List<String> pModelNames, Pageable pPageable) throws ModuleException;

    /**
     * Find the model attribute associations for a given entity type(or all if none is given)
     * @param pType
     * @return model attribute associations for a given entity type(or all if none is given)
     */
    Collection<ModelAttrAssoc> getModelAttrAssocsFor(EntityType pType);

    /**
     * @return the possible mappings between an attribute, computation plugin configurations and their metadata
     */
    List<TypeMetadataConfMapping> retrievePossibleMappingsForComputed();

    /**
     * Retrieve {@link AttributeModel} for the given {@link Model}s identifier.
     * @param modelIds List of {@link Model}s identifier
     * @param pageable
     * @return {@link AttributeModel}s
     */
    Page<AttributeModel> getAttributeModels(Set<Long> modelIds, Pageable pageable);
}
