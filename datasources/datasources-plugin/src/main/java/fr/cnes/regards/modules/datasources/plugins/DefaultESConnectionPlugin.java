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

package fr.cnes.regards.modules.datasources.plugins;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginDestroy;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IConnectionPlugin;

/**
 * Class DefaultESConnectionPlugin A default {@link Plugin} of type {@link IConnectionPlugin}. Allows to
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(id = "elasticsearch-connection", version = "1.0-SNAPSHOT", description = "Connection to a Elasticsearch engine",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class DefaultESConnectionPlugin implements IConnectionPlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultESConnectionPlugin.class);

    private static final String HOST_PARAM = "host";

    private static final String PORT_PARAM = "port";

    /**
     * The host
     */
    @PluginParameter(name = HOST_PARAM)
    private String host;

    /**
     * The (HTTP) port
     */
    @PluginParameter(name = PORT_PARAM)
    private int port;


    /**
     * The Rest client (low level API) used to connect to one or more node
     */
    private RestClient restClient;

    /**
     * The ES client (high level API) used to connect to one or more node
     */
    private RestHighLevelClient client;

    @Override
    public boolean testConnection() {
        try {
            return client.ping();
        } catch (IOException e) {
            LOG.error("Error while pinging Elasticsearch node", e);
            return false;
        }
    }

    @SuppressWarnings("resource")
    @PluginInit
    private void createRestClients() {
        restClient = RestClient.builder(new HttpHost(host, port)).build();
        client = new RestHighLevelClient(restClient);
    }

    @Override
    @PluginDestroy
    public void closeConnection() {
        try {
            restClient.close();
        } catch (IOException e) {
            LOG.warn("Error while closing client", e);
        }
    }

}
