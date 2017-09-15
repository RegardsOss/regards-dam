/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.entities.domain.event;

import fr.cnes.regards.framework.amqp.event.IPollable;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;

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