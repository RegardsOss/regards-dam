/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright.event;

import fr.cnes.regards.modules.dataaccess.domain.accessright.AbstractAccessRight;

/**
 *
 * Event to be sent once an {@link AbstractAccessRight} is created
 * 
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class AccessRightCreated extends AccessRightEvent {

    /**
     * @param pAccessRightId
     */
    public AccessRightCreated(Long pAccessRightId) {
        super(pAccessRightId);
    }

}
