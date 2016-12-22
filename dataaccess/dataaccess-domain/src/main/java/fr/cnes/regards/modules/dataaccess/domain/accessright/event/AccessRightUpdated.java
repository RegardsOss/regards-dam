/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright.event;

import fr.cnes.regards.modules.dataaccess.domain.accessright.AbstractAccessRight;

/**
 * Event to be sent once an {@link AbstractAccessRight} is updated
 * 
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class AccessRightUpdated extends AccessRightEvent {

    /**
     * @param pAccessRightId
     */
    public AccessRightUpdated(Long pAccessRightId) {
        super(pAccessRightId);
    }

}
