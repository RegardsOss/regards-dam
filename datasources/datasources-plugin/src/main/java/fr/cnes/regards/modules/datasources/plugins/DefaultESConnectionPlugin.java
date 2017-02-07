/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IConnectionPlugin;

/**
 * Class DefaultESConnectionPlugin
 *
 * A default {@link Plugin} of type {@link IConnectionPlugin}. Allows to
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(author = "CSSI", version = "1.0-SNAPSHOT", description = "Connection to a Elasticsearch engine")
public class DefaultESConnectionPlugin implements IConnectionPlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultESConnectionPlugin.class);

    private static final String HOST_PARAM = "host";

    private static final String PORT_PARAM = "port";

    private static final String CLUSTER_PARAM = "cluster";
    

    /**
     * The host
     */
    @PluginParameter(name = HOST_PARAM)
    private String host;

    /**
     * The port
     */
    @PluginParameter(name = PORT_PARAM)
    private int port;

    /**
     * The cluster
     */
    @PluginParameter(name = CLUSTER_PARAM)
    private String cluster;

    /**
     * The {@link TransportClient} used to connect to one or more node
     */
    private TransportClient client;

    @Override
    public boolean testConnection() {
        return !client.connectedNodes().isEmpty();
    }

    @SuppressWarnings("resource")
    @PluginInit
    private void createTransportClient() {
        Settings settings = Settings.EMPTY;

        if (cluster != null) {
            settings = Settings.builder().put("cluster.name", cluster).build();
        }

        try {
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
