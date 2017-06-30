/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Qualified interface for Collection entity service
 * @author oroussel
 */
public interface ICollectionService extends IEntityService<Collection> {

    DescriptionFile retrieveDescription(UniformResourceName collectionIpId) throws EntityNotFoundException;

    void removeDescription(UniformResourceName collectionIpId) throws EntityNotFoundException;
}
