/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.IPollable;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Entity event sent to AMQP indicating that the concerned entity has been created/modified/deleted
 * @author oroussel
 * @author Sylvain Vissiere-Guerinet
 */
@Event(target = Target.MICROSERVICE)
public class EntityEvent implements IPollable {

    /**
     * Business id identifying an entity
     */
    protected UniformResourceName[] ipIds;

    private EntityEvent() {
        super();
    }

    public EntityEvent(UniformResourceName... pIpIds) {
        this();
        ipIds = pIpIds;
    }

    public UniformResourceName[] getIpIds() {
        return ipIds;
    }

    @SuppressWarnings("unused")
    private void setIpIds(UniformResourceName... pIpIds) {
        ipIds = pIpIds;
    }

}