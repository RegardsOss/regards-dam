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

import fr.cnes.regards.modules.datasources.plugins.plugintypes.IConnectionPlugin;
import fr.cnes.regards.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.modules.plugins.annotations.PluginParameter;

/**
 * Class DefaultSqlConnectionPlugin
 *
 * A default {@link Plugin} of type {@link IConnectionPlugin}. Allows to
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(author = "CSSI", version = "1.0-SNAPSHOT", description = "Connection to a Sql database")
public class DefaultESConnectionPlugin implements IConnectionPlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultESConnectionPlugin.class);

    public static final String HOST = "host";

    public static final String PORT = "port";

    public static final String CLUSTER = "cluster";

    @PluginParameter(name = HOST)
    private String host;

    @PluginParameter(name = PORT)
    private int port;

    @PluginParameter(name = CLUSTER)
    private String cluster;

    private TransportClient client;

    @Override
    public boolean testConnection() {
        return client.connectedNodes().size() > 0;
    }

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
