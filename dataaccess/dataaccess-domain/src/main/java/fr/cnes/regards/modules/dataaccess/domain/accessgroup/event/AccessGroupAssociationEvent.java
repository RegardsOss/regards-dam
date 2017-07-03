/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessgroup.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;

/**
 * Publish when a group is associated to a user
 *
 * @author Marc Sordi
 */
@Event(target = Target.ALL)
public class AccessGroupAssociationEvent implements ISubscribable {

    /**
     * The source of the event
     */
    private AccessGroup accessGroup;

    private String userEmail;

    public AccessGroupAssociationEvent() {
        // Deserialization constructor
    }

    public AccessGroupAssociationEvent(AccessGroup accessGroup, String userEmail) {
        this.accessGroup = accessGroup;
        this.setUserEmail(userEmail);
    }

    public AccessGroup getAccessGroup() {
        return accessGroup;
    }

    public void setAccessGroup(AccessGroup pAccessGroup) {
        accessGroup = pAccessGroup;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String pUserEmail) {
        userEmail = pUserEmail;
    }
}
