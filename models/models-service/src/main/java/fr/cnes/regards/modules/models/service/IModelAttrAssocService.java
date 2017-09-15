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
package fr.cnes.regards.modules.models.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.TypeMetadataConfMapping;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.event.NewFragmentAttributeEvent;

/**
 * Model attribute association service description
 *
 * @author Marc Sordi
 * @author oroussel
 */
public interface IModelAttrAssocService extends ApplicationListener<NewFragmentAttributeEvent> {

    List<ModelAttrAssoc> getModelAttrAssocs(Long pModelId) throws ModuleException;

    ModelAttrAssoc bindAttributeToModel(Long pModelId, ModelAttrAssoc pModelAttribute) throws ModuleException;

    ModelAttrAssoc getModelAttrAssoc(Long pModelId, Long pAttributeId) throws ModuleException;

    ModelAttrAssoc getModelAttrAssoc(Long pModelId, AttributeModel pAttribute);

    ModelAttrAssoc updateModelAttribute(Long pModelId, Long pAttributeId, ModelAttrAssoc pModelAttribute)
            throws ModuleException;

    void unbindAttributeFromModel(Long pModelId, Long pAttributeId) throws ModuleException;

    List<ModelAttrAssoc> bindNSAttributeToModel(Long pModelId, Fragment pFragment) throws ModuleException;

    /**
     * Propagate a fragment update
     *
     * @param pFragmentId fragment updated
     * @throws ModuleException if error occurs!
     */
    void updateNSBind(AttributeModel added);

    void unbindNSAttributeToModel(Long pModelId, Long pFragmentId) throws ModuleException;

    Collection<ModelAttrAssoc> retrieveModelAttrAssocsByAttributeId(AttributeModel attr);

    Model duplicateModelAttrAssocs(Long pSourceModelId, Model pTargetModel) throws ModuleException;

    Set<ModelAttrAssoc> getComputedAttributes(Long pId);

    Page<AttributeModel> getAttributeModels(List<Long> pModelIds, Pageable pPageable) throws ModuleException;

    Collection<ModelAttrAssoc> getModelAttrAssocsFor(EntityType pType);

    List<TypeMetadataConfMapping> retrievePossibleMappingsForComputed();
}
