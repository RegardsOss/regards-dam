/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;

/**
 * Qualified interface for Collection entity service
 * @author oroussel
 */
public interface ICollectionService extends IEntityService<Collection> {

    DescriptionFile retrieveDescription(Long pCollectionId) throws EntityNotFoundException;

    void removeDescription(Long collectionId) throws EntityNotFoundException;
}
