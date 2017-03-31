/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.List;
import java.util.Set;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;

/**
 * Unparameterized entity service description. This is to be used when the entity type is unknown (ex. CrawlerService)
 *
 * @author oroussel
 */
public interface IEntitiesService {

    /**
     * Load entity by IpId with all its relations
     *
     * @param ipId business id
     * @return entity with all its relations (ie. groups, tags, ...) or null if entity doesn't exists
     */
    AbstractEntity loadWithRelations(UniformResourceName ipId);

    /**
     * Load entities by IpId with all their relations
     *
     * @param ipIds business ids
     * @return entities with all its relations (ie. groups, tags, ...) or empty list
     */
    List<AbstractEntity> loadAllWithRelations(UniformResourceName... ipIds);

    /**
     * Retrieve and instanciate the plugins needed to compute all the computed attributes of an entity. We may not be
     * able to compute the attributes here because of pagination of {@link DataObject} cf. CrawlerService, that's why we
     * are just instanciating the plugins.
     *
     * @param pEntity entity we are interrested to get computation plugins
     * @return instanciated plugins so computation can be executed
     */
    <T extends IComputedAttribute<DataObject, ?>> Set<T> getComputationPlugins(Dataset pEntity);
}
