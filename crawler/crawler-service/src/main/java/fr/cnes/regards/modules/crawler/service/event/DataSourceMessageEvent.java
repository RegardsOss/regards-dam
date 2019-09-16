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
package fr.cnes.regards.modules.crawler.service.event;

import org.springframework.context.ApplicationEvent;

/**
 * Message event used to transmit message from CrawlerService to IngesterService while ingesting data in order to make
 * ingester updating DatasourceIngestion (these messages are enqueued into stacktrace attribute)
 * @author oroussel
 */
public class DataSourceMessageEvent extends ApplicationEvent {

    private final String tenant;

    private final String message;

    private final String dataSourceId;

    public DataSourceMessageEvent(Object source, String tenant, String message, String dataSourceId) {
        super(source);
        this.tenant = tenant;
        this.message = message;
        this.dataSourceId = dataSourceId;
    }

    public String getMessage() {
        return message;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public String getTenant() {
        return tenant;
    }
}
