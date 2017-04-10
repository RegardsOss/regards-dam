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

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.plugin.CountElementAttribute;
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

    private final IDatasetRepository datasetRepository;

    private final IAbstractEntityRepository<AbstractEntity> entityRepository;

    private final IModelAttrAssocService modelAttributeService;

    private final IPluginService pluginService;

    public EntitiesService(IDatasetRepository pDatasetRepository,
            IAbstractEntityRepository<AbstractEntity> pEntityRepository, IModelAttrAssocService pModelAttributeService,
            IPluginService pPluginService) {
        super();
        datasetRepository = pDatasetRepository;
        entityRepository = pEntityRepository;
        modelAttributeService = pModelAttributeService;
        pluginService = pPluginService;
        pluginService.addPluginPackage(CountElementAttribute.class.getPackage().getName());
    }

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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IComputedAttribute<Dataset, ?>> Set<T> getComputationPlugins(Dataset pDataset) {

        Set<ModelAttrAssoc> computedAttributes = modelAttributeService
                .getComputedAttributes(pDataset.getModel().getId());
        Set<T> computationPlugins = new HashSet<>();
        computedAttributes.forEach(attr -> {
            try {
                IComputedAttribute<?, ?> plugin = pluginService.getPlugin(attr.getComputationConf());
                // here we have a plugin with no idea of the type of the generic parameter used by the "compute" method,
                // lets check that it is a IComputedAttribute<Dataset,?>
                plugin.getClass().getMethod("compute", Dataset.class);
                // if no exception has been thrown then the method exist and we are in presence of a
                // IComputedAttribute<Dataset, ?>
                computationPlugins.add((T) plugin);
            } catch (ModuleException e) {
                // rethrow as a runtime because anyway there is nothing we can do there, if the plugin cannot be
                // instantiate the system should set itself to maintenance mode!
                throw new RuntimeException(e); // NOSONAR
            } catch (SecurityException e) {
                // This exception should not happen. so if it does lets put the system into maintenance mode
                throw new RuntimeException(e); // NOSONAR
            } catch (NoSuchMethodException e) {
                // this is a normal exception in the logic of the method: to know if we have an
                // IComputedAttribute<Dataset, ?> we check if a method compute(Dataset) is defined, if not then we just
                // don't consider this plugin
            }
        });
        return computationPlugins;
    }

}
