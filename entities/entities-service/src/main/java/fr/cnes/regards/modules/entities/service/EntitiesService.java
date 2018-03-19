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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.plugin.CountPlugin;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;

/**
 * Unparameterized entity service. This service is used when the entity type is unknown (ex. CrawlerService)
 *
 * @author oroussel
 */
@Service
@MultitenantTransactional
public class EntitiesService implements IEntitiesService {



    @Autowired
    private IDatasetRepository datasetRepository;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity> entityRepository;

    @Autowired
    private IModelAttrAssocService modelAttributeService;

    @Autowired
    private IPluginService pluginService;

    /**
     * {@link ICollectionRepository} instance
     */
    @Autowired
    private ICollectionRepository collectionRepository;

    public EntitiesService() {
        super();
    }

    @PostConstruct
    public void initPluginPackage() {
        pluginService.addPluginPackage(CountPlugin.class.getPackage().getName());
    }

    @Override
    public AbstractEntity loadWithRelations(UniformResourceName pIpId) {
        // Particular case on datasets and collections which contains more relations
        if (pIpId.getEntityType() == EntityType.DATASET) {
            return datasetRepository.findByIpId(pIpId);
        }
        if (pIpId.getEntityType() == EntityType.COLLECTION) {
            return collectionRepository.findByIpId(pIpId);
        }
        return entityRepository.findByIpId(pIpId);
    }

    @Override
    public List<AbstractEntity> loadAllWithRelations(UniformResourceName... pIpIds) {
        List<AbstractEntity> entities = new ArrayList<>(pIpIds.length);
        Set<UniformResourceName> dsUrns = Arrays.stream(pIpIds)
                .filter(ipId -> ipId.getEntityType() == EntityType.DATASET).collect(Collectors.toSet());
        if (!dsUrns.isEmpty()) {
            entities.addAll(datasetRepository.findByIpIdIn(dsUrns));
        }
        Set<UniformResourceName> otherUrns = Arrays.stream(pIpIds)
                .filter(ipId -> ipId.getEntityType() != EntityType.DATASET).collect(Collectors.toSet());
        if (!otherUrns.isEmpty()) {
            entities.addAll(entityRepository.findByIpIdIn(otherUrns));
        }
        return entities;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IComputedAttribute<Dataset, ?>> Set<T> getComputationPlugins(Dataset pDataset) {

        Set<ModelAttrAssoc> computedAttributes = modelAttributeService
                .getComputedAttributes(pDataset.getModel().getId());
        Set<T> computationPlugins = new HashSet<>();
        try {
            for (ModelAttrAssoc attr : computedAttributes) {
                try {
                    PluginParameter resultFragmentName = new PluginParameter(IComputedAttribute.RESULT_FRAGMENT_NAME, attr.getAttribute().getFragment().getName());
                    resultFragmentName.setOnlyDynamic(true);
                    PluginParameter resultAttrName = new PluginParameter(IComputedAttribute.RESULT_ATTRIBUTE_NAME, attr.getAttribute().getName());
                    resultAttrName.setOnlyDynamic(true);
                    IComputedAttribute<?, ?> plugin = pluginService.getPlugin(attr.getComputationConf().getId(), resultAttrName, resultFragmentName);
                    // here we have a plugin with no idea of the type of the generic parameter used by the "compute"
                    // method, lets check that it is a IComputedAttribute<Dataset,?>
                    plugin.getClass().getMethod("compute", Dataset.class);
                    // if no exception has been thrown then the method exist and we are in presence of a
                    // IComputedAttribute<Dataset, ?>
                    computationPlugins.add((T) plugin);
                } catch (NoSuchMethodException e) { // NOSONAR
                    // this is a normal exception in the logic of the method: to know if we have an
                    // IComputedAttribute<Dataset, ?> we check if a method compute(Dataset) is defined, if not then we
                    // just don't consider this plugin
                    // FIXME trace something!
                }
            }
        } catch (ModuleException e) {
            // Rethrow as a runtime because anyway there is nothing we can do there, if the plugin cannot be
            // instantiated the system should set itself to maintenance mode!
            throw new RsRuntimeException(e);
        } catch (SecurityException e) {
            // This exception should not happen. so if it does lets put the system into maintenance mode
            throw new RsRuntimeException(e);
        }
        return computationPlugins;
    }

}
