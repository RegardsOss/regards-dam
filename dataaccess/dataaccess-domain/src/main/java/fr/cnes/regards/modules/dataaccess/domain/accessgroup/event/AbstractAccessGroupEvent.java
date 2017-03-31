/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessgroup.event;

import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;

/**
 * {@link AccessGroup} event common information
 *
 * @author Xavier-Alexandre Brochard
 */
public abstract class AbstractAccessGroupEvent implements ISubscribable {

    /**
     * The source of the event
     */
    private final AccessGroup accessGroup;

    /**
     * @param pAccessGroup the source of the event
     */
    public AbstractAccessGroupEvent(AccessGroup pAccessGroup) {
        super();
        accessGroup = pAccessGroup;
    }

    /**
     * @return the accessGroup
     */
    public AccessGroup getAccessGroup() {
        return accessGroup;
    }

}
