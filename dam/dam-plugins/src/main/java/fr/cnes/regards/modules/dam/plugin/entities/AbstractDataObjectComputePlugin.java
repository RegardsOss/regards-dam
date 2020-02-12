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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.dao.spatial.ProjectGeoSettings;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.model.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.model.domain.IComputedAttribute;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.Fragment;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.model.dto.properties.ObjectProperty;

/**
 * Implementation of {@link IComputedAttribute} plugin interface.
 * @param <R> type of the result attribute value
 * @author Sylvain Vissiere-Guerinet
 */
public abstract class AbstractDataObjectComputePlugin<R> implements IComputedAttribute<Dataset, R> {

    /**
     * The plugin parameter name for the attribute name
     */
    public static final String PARAMETER_ATTRIBUTE_NAME = "parameterAttributeName";

    /**
     * The plugin parameter name for the fragment name
     */
    public static final String PARAMETER_FRAGMENT_NAME = "parameterAttributeFragmentName";

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected IEsRepository esRepo;

    @Autowired
    protected IRuntimeTenantResolver tenantResolver;

    @Autowired
    protected IAttributeModelRepository attModelRepos;

    @Autowired
    protected ProjectGeoSettings projectGeoSettings;

    protected AttributeModel parameterAttribute;

    private AttributeModel attributeToCompute;

    @PluginParameter(name = RESULT_ATTRIBUTE_NAME, label = "Result attribute name",
            description = "Name of attribute to compute (ie result attribute).", unconfigurable = true)
    protected String attributeToComputeName;

    @PluginParameter(name = RESULT_FRAGMENT_NAME, label = "Result fragment name",
            description = "Name of attribute to compute fragment. If computed attribute belongs to default fragment, this value can be set to null.",
            optional = true, unconfigurable = true)
    protected String attributeToComputeFragmentName;

    protected R result;

    @Override
    public R getResult() {
        return result;
    }

    protected void init(String attributeToComputeName, String attributeToComputeFragmentName,
            String parameterAttributeName, String parameterAttributeFragmentName) {
        attributeToCompute = attModelRepos
                .findByNameAndFragmentName(attributeToComputeName,
                                           Strings.isNullOrEmpty(attributeToComputeFragmentName)
                                                   ? Fragment.getDefaultName()
                                                   : attributeToComputeFragmentName);
        if (attributeToCompute == null) {
            if (!Strings.isNullOrEmpty(attributeToComputeFragmentName)) {
                throw new IllegalArgumentException(
                        String.format("Cannot find computed attribute '%s'.'%s'", attributeToComputeFragmentName,
                                      attributeToComputeName));
            } else {
                throw new IllegalArgumentException(
                        String.format("Cannot find computed attribute '%s'", attributeToComputeName));
            }
        }
        parameterAttribute = attModelRepos
                .findByNameAndFragmentName(parameterAttributeName,
                                           Strings.isNullOrEmpty(parameterAttributeFragmentName)
                                                   ? Fragment.getDefaultName()
                                                   : parameterAttributeFragmentName);
        if (parameterAttribute == null) {
            if (!Strings.isNullOrEmpty(parameterAttributeFragmentName)) {
                throw new IllegalArgumentException(
                        String.format("Cannot find parameter attribute '%s'.'%s'", parameterAttributeFragmentName,
                                      parameterAttributeName));
            } else {
                throw new IllegalArgumentException(
                        String.format("Cannot find parameter attribute '%s'", parameterAttributeName));
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
        esRepo.searchAll(searchKey, this.doCompute(), dataset.getSubsettingClause());
        log.debug("Attribute {} computed for Dataset {}. Result: {}", parameterAttribute.getFullJsonPath(),
                  dataset.getIpId().toString(), result);
    }

    @Override
    public AttributeModel getAttributeToCompute() {
        return attributeToCompute;
    }

    protected abstract Consumer<DataObject> doCompute();

    /**
     * Extract the property of which name and eventually fragment name are given
     */
    protected Optional<IProperty<?>> extractProperty(DataObject object) { // NOSONAR
        if (parameterAttribute.getFragment().isDefaultFragment()) {
            // the attribute is in the default fragment so it has at the root level of properties
            return Optional.ofNullable(object.getProperty(parameterAttribute.getName()));
        }
        // the attribute is in a fragment so :
        // filter the fragment property then filter the right property on fragment properties
        com.google.common.base.Optional<ObjectProperty> fragmentOpt = com.google.common.base.Optional
                .fromNullable((ObjectProperty) object.getProperty(parameterAttribute.getFragment().getName()));
        return fragmentOpt.isPresent()
                ? Iterables.tryFind(fragmentOpt.get().getValue(), p -> p.getName().equals(parameterAttribute.getName()))
                        .toJavaUtil()
                : Optional.empty();

    }

}
