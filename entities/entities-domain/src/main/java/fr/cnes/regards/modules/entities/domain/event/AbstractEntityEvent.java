/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.event;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public abstract class AbstractEntityEvent {

    protected UniformResourceName ipId;

    /**
     *
     */
    public AbstractEntityEvent() {
        super();
    }

    public AbstractEntityEvent(UniformResourceName pIpId) {
        this();
        ipId = pIpId;
    }

    public UniformResourceName getIpId() {
        return ipId;
    }

    public void setIpId(UniformResourceName pIpId) {
        ipId = pIpId;
    }

}