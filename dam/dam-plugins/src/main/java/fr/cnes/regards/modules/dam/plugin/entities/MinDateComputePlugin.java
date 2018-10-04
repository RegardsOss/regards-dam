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
package fr.cnes.regards.modules.dam.plugin.entities;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.dam.dao.models.IAttributeModelRepository;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.attribute.AbstractAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.DateAttribute;
import fr.cnes.regards.modules.dam.domain.models.ComputationPlugin;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;

/**
 * This IComputedAttribute implementation allows to compute the minimum of a {@link DateAttribute} according to a
 * collection of {@link DataObject} using the same DateAttribute name
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "MinDateComputePlugin",
        description = "allows to compute the minimum of a DateAttribute according to a collection of data",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss", version = "1.0.0")
@ComputationPlugin(supportedType = AttributeType.DATE_ISO8601)
public class MinDateComputePlugin extends AbstractDataObjectComputePlugin<OffsetDateTime> {

    @Autowired
    private IEsRepository esRepo;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private IAttributeModelRepository attModelRepos;

    @PluginParameter(name = PARAMETER_ATTRIBUTE_NAME, label = "Parameter attribute name",
            description = "Name of parameter attribute used to compute result attribute.")
    private String parameterAttributeName;

    @PluginParameter(name = PARAMETER_FRAGMENT_NAME, label = "Parameter fragment name",
            description = "Name of the parameter attribute fragment. If the parameter attribute belongs to the default fragment, this value can be set to null.",
            optional = true)
    private String parameterAttributeFragmentName;

    /**
     * Plugin initialization method
     */
    @PluginInit
    public void init() {
        initAbstract(esRepo, attModelRepos, tenantResolver);
        super.init(attributeToComputeName, attributeToComputeFragmentName, parameterAttributeName,
                   parameterAttributeFragmentName);
    }

    /**
     * @param dataset dataset on which the attribute, once computed, will be added. This allows us to know which
     * DataObject should be used.
     */
    @Override
    public void compute(Dataset dataset) {
        result = null;
        // create the search
        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(EntityType.DATA.toString(), DataObject.class);
        searchKey.setSearchIndex(tenantResolver.getTenant());
        result = esRepo.minDate(searchKey, dataset.getSubsettingClause(), parameterAttribute.getFullJsonPath());
        log.debug("Attribute {} computed for Dataset {}. Result: {}", parameterAttribute.getFullJsonPath(),
                  dataset.getIpId().toString(), result);
    }

    private void getMinDate(Optional<AbstractAttribute<?>> parameterOpt) {
        if (parameterOpt.isPresent()) {
            OffsetDateTime value = (OffsetDateTime) parameterOpt.get().getValue();
            if (value != null) {
                if (result == null) {
                    result = value;
                } else {
                    result = value.isBefore(result) ? value : result;
                }
            }
        }
    }

    @Override
    protected Consumer<DataObject> doCompute() {
        return object -> getMinDate(extractProperty(object));
    }

}
