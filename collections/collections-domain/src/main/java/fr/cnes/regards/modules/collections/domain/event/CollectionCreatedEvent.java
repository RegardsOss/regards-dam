/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.domain.event;

import fr.cnes.regards.modules.entities.domain.event.AbstractEntityEvent;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class CollectionCreatedEvent extends AbstractEntityEvent {

    /**
     * @param pId
     *            ID of the newly created {@link CollectionCreatedEvent}
     */
    public CollectionCreatedEvent(UniformResourceName pId) {
        super(pId);

    }

}
