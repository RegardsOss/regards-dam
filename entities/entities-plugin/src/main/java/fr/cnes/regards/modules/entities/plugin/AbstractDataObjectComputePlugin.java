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

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.StaticProperties;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 * Implementation of {@link IComputedAttribute} plugin interface.
 * @param <R> type of the result attribute value
 * @author Sylvain Vissiere-Guerinet
 */
public abstract class AbstractDataObjectComputePlugin<R> implements IComputedAttribute<Dataset, R> {

    public static final String PARAMETER_ATTRIBUTE_NAME = "parameterAttributeName";

    public static final String PARAMETER_FRAGMENT_NAME = "parameterAttributeFragmentName";

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractDataObjectComputePlugin.class);

    private IEsRepository esRepo;

    protected IAttributeModelRepository attModelRepos;

    private IRuntimeTenantResolver tenantResolver;

    protected AttributeModel parameterAttribute;

    private AttributeModel attributeToCompute;

    protected R result;

    @Override
    public R getResult() {
        return result;
    }

    /**
     * Each of those beans cannot be wired into the abstract so they have to be autowired into plugin implementation and
     * given thanks to this method. Doing so fully initialize the abstraction.
     */
    protected void initAbstract(IEsRepository esRepo, IAttributeModelRepository attModelRepos,
            IRuntimeTenantResolver tenantResolver) {
        this.esRepo = esRepo;
        this.attModelRepos = attModelRepos;
        this.tenantResolver = tenantResolver;
    }

    protected void init(String attributeToComputeName, String attributeToComputeFragmentName,
            String parameterAttributeName, String parameterAttributeFragmentName) {
        attributeToCompute = attModelRepos.findByNameAndFragmentName(attributeToComputeName, (Strings.isNullOrEmpty(
                attributeToComputeFragmentName) ? Fragment.getDefaultName() : attributeToComputeFragmentName));
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
        parameterAttribute = attModelRepos.findByNameAndFragmentName(parameterAttributeName, (Strings.isNullOrEmpty(
                parameterAttributeFragmentName) ? Fragment.getDefaultName() : parameterAttributeFragmentName));
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
        parameterAttribute.buildJsonPath(StaticProperties.PROPERTIES);

    }

    /**
     * @param dataset dataset on which the attribute, once computed, will be added. This allows us to know which
     * DataObject should be used.
     */
    @Override
    public void compute(Dataset dataset) {
        result = null;
        // create the search
        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(tenantResolver.getTenant(),
                                                                      EntityType.DATA.toString(), DataObject.class);
        esRepo.searchAll(searchKey, this.doCompute(), dataset.getSubsettingClause());
        LOG.debug("Attribute {} computed for Dataset {}. Result: {}", parameterAttribute.getJsonPath(),
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
    protected Optional<AbstractAttribute<?>> extractProperty(DataObject object) { //NOSONAR
        if (parameterAttribute.getFragment().isDefaultFragment()) {
            // the attribute is in the default fragment so it has at the root level of properties
            return object.getProperties().stream().filter(p -> p.getName().equals(parameterAttribute.getName()))
                    .findFirst();
        }
        // the attribute is in a fragment so :
        // filter the fragment property then filter the right property on fragment properties
        return object.getProperties().stream().filter(p -> (p instanceof ObjectAttribute) && p.getName()
                .equals(parameterAttribute.getFragment().getName())).limit(1) // Only one fragment with searched name
                .flatMap(fragment -> ((ObjectAttribute) fragment).getValue().stream())
                .filter(p -> p.getName().equals(parameterAttribute.getName())).findFirst();
    }

}
