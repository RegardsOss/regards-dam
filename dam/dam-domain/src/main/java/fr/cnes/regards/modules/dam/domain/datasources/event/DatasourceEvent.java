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
package fr.cnes.regards.modules.dam.domain.datasources.event;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;

/**
 * Event to inform consumers that a datasource has been modified.
 *
 * @author Sébastien Binda
 *
 */
@Event(target = Target.MICROSERVICE)
public class DatasourceEvent implements ISubscribable {

    private Long datasourceId;

    private DatasourceEventType type;

    public static DatasourceEvent buildDeleted(Long datasourceId) {
        DatasourceEvent de = new DatasourceEvent();
        de.datasourceId = datasourceId;
        de.type = DatasourceEventType.DELETED;
        return de;
    }

    public Long getDatasourceId() {
        return datasourceId;
    }

    public DatasourceEventType getType() {
        return type;
    }

}
