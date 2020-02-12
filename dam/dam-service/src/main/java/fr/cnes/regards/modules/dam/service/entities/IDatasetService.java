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
package fr.cnes.regards.modules.dam.service.entities;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterionVisitor;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;

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
     * @param pUrns {@link UniformResourceName}s
     * @param modelNames
     * @param pPageable
     * @return {@link AttributeModel}s
     * @throws ModuleException
     */
    Page<AttributeModel> getDataAttributeModels(Set<UniformResourceName> pUrns, Set<String> modelNames, Pageable pPageable)
            throws ModuleException;

    /**
     * Retrieve {@link AttributeModel}s associated to the given {@link Dataset}s or {@link Model}s given.
     * @param pUrns
     * @param modelNames
     * @param pPageable
     * @return {@link AttributeModel}s
     * @throws ModuleException
     */
    Page<AttributeModel> getAttributeModels(Set<UniformResourceName> pUrns, Set<String> modelNames, Pageable pPageable)
            throws ModuleException;

    /**
     * Build a criterion visitor allowing us to check if a criterion is valid or not
     *
     * @param dataModelName modelName towards which we should check coherence
     * @return visitor to perform a coherence check
     * @throws ModuleException if the model cannot be retrieve
     */
    ICriterionVisitor<Boolean> getSubsettingCoherenceVisitor(String dataModelName) throws ModuleException;

    /**
     * Get the number of datasets associated to a given datasource plugin configuration.
     * @param dataSourcePluginConfId identifier of the {@link PluginConfiguration} of the datasource
     * @return number of datasets.
     */
    Long countByDataSource(Long dataSourcePluginConfId);

    Set<Dataset> findAllByModel(Long modelId);
}