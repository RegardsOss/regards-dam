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
package fr.cnes.regards.modules.entities.plugin;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.domain.ComputationPlugin;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 * This implementation allows to compute the number of {@link DataObject} of a {@link Dataset}
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "CountPlugin", description = "allows to compute the number of data of a Dataset", author = "REGARDS Team",
        contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI", url = "https://github.com/RegardsOss",
        version = "1.0.0")
@ComputationPlugin(supportedType = AttributeType.LONG)
public class CountPlugin implements IComputedAttribute<Dataset, Long> {

    @Autowired
    private IEsRepository esRepo;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    protected IAttributeModelRepository attModelRepos;

    @PluginParameter(name = RESULT_ATTRIBUTE_NAME, label = "Result attribute name",
            description = "Name of attribute to compute (ie result attribute).", unconfigurable = true)
    private String attributeToComputeName;

    @PluginParameter(name = RESULT_FRAGMENT_NAME, label = "Result fragment name",
            description = "Name of attribute to compute fragment. If computed attribute belongs to "
                    + "default fragment, this value can be set to null.", optional = true, unconfigurable = true)
    private String attributeToComputeFragmentName;

    private AttributeModel attributeToCompute;

    private long count = 0L;

    /**
     * Plugin initialization method
     */
    @PluginInit
    public void init() {
        attributeToCompute = attModelRepos.findByNameAndFragmentName(attributeToComputeName, Strings.isNullOrEmpty(
                attributeToComputeFragmentName) ? Fragment.getDefaultName() : attributeToComputeFragmentName);
    }

    @Override
    public Long getResult() {
        return count;
    }

    @Override
    public void compute(Dataset dataset) {
        // create the search
        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(tenantResolver.getTenant(),
                                                                      EntityType.DATA.toString(), DataObject.class);
        count = esRepo.count(searchKey, dataset.getSubsettingClause());
    }

    @Override
    public AttributeModel getAttributeToCompute() {
        return attributeToCompute;
    }

}
