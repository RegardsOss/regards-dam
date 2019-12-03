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
package fr.cnes.regards.modules.dam.plugin.entities;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.dao.spatial.ProjectGeoSettings;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.model.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.model.domain.ComputationPlugin;
import fr.cnes.regards.modules.model.domain.IComputedAttribute;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.Fragment;
import fr.cnes.regards.modules.model.domain.models.PluginComputationIdentifierEnum;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * This implementation allows to compute the number of {@link DataObject} of a {@link Dataset}
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = PluginComputationIdentifierEnum.COUNT_VALUE,
        description = "allows to compute the number of data of a Dataset", author = "REGARDS Team",
        contact = "regards@c-s.fr", license = "GPLv3", owner = "CSSI", url = "https://github.com/RegardsOss",
        version = "1.0.0")
@ComputationPlugin(supportedType = PropertyType.LONG)
public class CountPlugin implements IComputedAttribute<Dataset, Long> {

    @Autowired
    private IEsRepository esRepo;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private IAttributeModelRepository attModelRepos;

    @Autowired
    private ProjectGeoSettings projectGeoSettings;

    @PluginParameter(name = RESULT_ATTRIBUTE_NAME, label = "Result attribute name",
            description = "Name of attribute to compute (ie result attribute).", unconfigurable = true)
    private String attributeToComputeName;

    @PluginParameter(name = RESULT_FRAGMENT_NAME, label = "Result fragment name",
            description = "Name of attribute to compute fragment. If computed attribute belongs to "
                    + "default fragment, this value can be set to null.",
            optional = true, unconfigurable = true)
    private String attributeToComputeFragmentName;

    private AttributeModel attributeToCompute;

    private long count = 0L;

    /**
     * Plugin initialization method
     */
    @PluginInit
    public void init() {
        attributeToCompute = attModelRepos
                .findByNameAndFragmentName(attributeToComputeName,
                                           Strings.isNullOrEmpty(attributeToComputeFragmentName)
                                                   ? Fragment.getDefaultName()
                                                   : attributeToComputeFragmentName);
    }

    @Override
    public Long getResult() {
        return count;
    }

    @Override
    public void compute(Dataset dataset) {
        // create the search
        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(EntityType.DATA.toString(), DataObject.class);
        searchKey.setSearchIndex(tenantResolver.getTenant());
        searchKey.setCrs(projectGeoSettings.getCrs());
        count = esRepo.count(searchKey, dataset.getSubsettingClause());
    }

    @Override
    public AttributeModel getAttributeToCompute() {
        return attributeToCompute;
    }

}
