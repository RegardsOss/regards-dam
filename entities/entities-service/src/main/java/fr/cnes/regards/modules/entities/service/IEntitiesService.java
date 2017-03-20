package fr.cnes.regards.modules.entities.service;

import java.util.List;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Not parameterized entity service
 * @author oroussel
 */
public interface IEntitiesService {

    /**
     * Load entity by IpId with all its relations
     * @param ipId business id
     * @return entity with all its relations (ie. groups, tags, ...) or null if entity doesn't exists
     */
    AbstractEntity loadWithRelations(UniformResourceName ipId);

    /**
     * Load entities by IpId with all their relations
     * @param ipIds business ids
     * @return entities with all its relations (ie. groups, tags, ...) or empty list
     */
    List<AbstractEntity> loadAllWithRelations(UniformResourceName... ipIds);
}
