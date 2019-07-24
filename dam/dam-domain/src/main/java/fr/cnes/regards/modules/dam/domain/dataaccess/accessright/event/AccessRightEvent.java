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
package fr.cnes.regards.modules.dam.domain.dataaccess.accessright.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;

/**
 * Access right event.
 *
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Event(target = Target.ALL)
public class AccessRightEvent implements ISubscribable {

    private UniformResourceName datasetIpId;

    private AccessRightEventType eventType;

    @SuppressWarnings("unused")
    private AccessRightEvent() {
        super();
    }

    public AccessRightEvent(UniformResourceName datasetIpId, AccessRightEventType eventType) {
        this.datasetIpId = datasetIpId;
        this.eventType = eventType;
    }

    public UniformResourceName getDatasetIpId() {
        return datasetIpId;
    }

    public AccessRightEventType getEventType() {
        return eventType;
    }
}
