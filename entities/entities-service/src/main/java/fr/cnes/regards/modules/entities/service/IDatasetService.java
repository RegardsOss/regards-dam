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
package fr.cnes.regards.modules.entities.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.service.visitor.SubsettingCoherenceVisitor;
import fr.cnes.regards.framework.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * Qualified interface for Dataset entity service
 *
 * @author oroussel
 */
public interface IDatasetService extends IEntityService<Dataset> {

    /**
     * Extract the AttributeModel of {@link DataObject} that can be contained into datasets. <br/>
     * If pUrns is null or empty AND pModelName is null then the scope of datasets is not restrained.<br/>
     * If pUrns is not null and not empty AND pModelName is not null then we only consider pModelName.<br/>
     * If pModelName is not null then the scope of datasets is restrained to all datasets complient with the given
     * model.<br/>
     * If pUrns is not null and not empty AND pModelName is null then the scope of datasets is restrained to all the
     * datasets represented by the given urns
     *
     * @param pPageable
     */
    Page<AttributeModel> getDataAttributeModels(Set<UniformResourceName> pUrns, Set<Long> pModelIds, Pageable pPageable)
            throws ModuleException;

    DescriptionFile retrieveDescription(UniformResourceName datasetIpId) throws EntityNotFoundException;

    void removeDescription(UniformResourceName datasetIpId) throws EntityNotFoundException;

    /**
     * Build a criterion visitor allowing us to check if a criterion is valid or not
     *
     * @param dataModelId modelId towards which we should check coherence
     * @return visitor to perform a coherence check
     * @throws ModuleException if the model cannot be retrieve
     */
    SubsettingCoherenceVisitor getSubsettingCoherenceVisitor(Long dataModelId) throws ModuleException;
}