/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;

/**
 * Published when a group is dissociated from a user
 *
 * @author Marc Sordi
 */
@Event(target = Target.ALL)
public class AccessGroupDissociationEvent implements ISubscribable {

    /**
     * The source of the event
     */
    private AccessGroup accessGroup;

    private String userEmail;

    public AccessGroupDissociationEvent() {
        // Deserialization constructor
    }

    public AccessGroupDissociationEvent(AccessGroup accessGroup, String userEmail) {
        this.accessGroup = accessGroup;
        this.userEmail = userEmail;
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
