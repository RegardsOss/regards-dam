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

import java.util.Optional;
import java.util.function.Consumer;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.model.domain.ComputationPlugin;
import fr.cnes.regards.modules.model.domain.models.PluginComputationIdentifierEnum;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.model.dto.properties.IntegerProperty;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * This Implementation of IComputedAttribute allows to compute the sum of {@link IntegerProperty} according to a
 * collection of {@link DataObject} using the same IntegerAttribute name
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = PluginComputationIdentifierEnum.INT_SUM_COUNT_VALUE, version = "1.0.0",
        description = "allows to compute the sum of IntegerAttribute according to a collection of data using the same IntegerAttribute name",
        author = "REGARDS Team", contact = "regards@c-s.fr", license = "GPLv3", owner = "CSSI",
        url = "https://github.com/RegardsOss")
@ComputationPlugin(supportedType = PropertyType.INTEGER)
public class IntSumComputePlugin extends AbstractDataObjectComputePlugin<Integer> {

    @PluginParameter(name = PARAMETER_ATTRIBUTE_NAME, label = "Parameter attribute name",
            description = "Name of parameter attribute used to compute result attribute.")
    private String parameterAttributeName;

    @PluginParameter(name = PARAMETER_FRAGMENT_NAME, label = "Parameter fragment name",
            description = "Name of parameter attribute fragment. If parameter attribute belongs to default fragment, leave this field empty.",
            optional = true)
    private String parameterAttributeFragmentName;

    /**
     * Plugin initialization method
     */
    @PluginInit
    public void init() {
        super.init(attributeToComputeName, attributeToComputeFragmentName, parameterAttributeName,
                   parameterAttributeFragmentName);
        super.result = 0;
    }

    private void doSum(Optional<IProperty<?>> propertyOpt) {
        if (propertyOpt.isPresent()) {
            Integer value = ((Number) propertyOpt.get().getValue()).intValue();
            if (value != null) {
                super.result += value;
            }
        }
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
        searchKey.setCrs(projectGeoSettings.getCrs());
        Double doubleResult = esRepo.sum(searchKey, dataset.getSubsettingClause(),
                                         parameterAttribute.getFullJsonPath());
        result = doubleResult.intValue();
        log.debug("Attribute {} computed for Dataset {}. Result: {}", parameterAttribute.getFullJsonPath(),
                  dataset.getIpId().toString(), result);
    }

    @Override
    protected Consumer<DataObject> doCompute() {
        super.result = 0;
        return object -> doSum(extractProperty(object));
    }

}
