/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.event;

import fr.cnes.regards.framework.amqp.event.IPollable;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Microservice specific abstract entity event (@see CrawlerService) sent to AMQP indicating that the concerned entity has been
 * created/modified/deleted
 * @author oroussel
 * @author Sylvain Vissiere-Guerinet
 */
public abstract class AbstractEntityEvent implements IPollable {

    /**
     * Business id identifying an entity
     */
    private UniformResourceName[] ipIds;

    private AbstractEntityEvent() {
        super();
    }

    public AbstractEntityEvent(UniformResourceName... pIpIds) {
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