/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;

/**
 * Unparameterized entity service. This service is used when the entity type is unknown (ex. CrawlerService)
 *
 * @author oroussel
 */
@Service
public class EntitiesService implements IEntitiesService {

    @Autowired
    private IDatasetRepository datasetRepository;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity> entityRepository;

    @Autowired
    private IModelAttrAssocService modelAttributeService;

    @Autowired
    private IPluginService pluginService;

    @Override
    public AbstractEntity loadWithRelations(UniformResourceName pIpId) {
        // Particular case on datasets which contains more relations
        if (pIpId.getEntityType() == EntityType.DATASET) {
            return datasetRepository.findByIpId(pIpId);
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

    @Override
    public <T extends IComputedAttribute<?>> Set<T> getComputationPlugins(Dataset pDataset) {

        Set<ModelAttrAssoc> computedAttributes = modelAttributeService
                .getComputedAttributes(pDataset.getModel().getId());
        Set<T> computationPlugins = new HashSet<>();
        computedAttributes.forEach(attr -> {
            try {
                computationPlugins.add(pluginService.getPlugin(attr.getComputationConf()));
            } catch (ModuleException e) {
                // rethrow as a runtime because anyway there is nothing we can do there, if the plugin cannot be
                // instiate the system should set itself to maintenance mode!
                throw new RuntimeException(e); // NOSONAR
            }
        });
        return computationPlugins;

    }

}
