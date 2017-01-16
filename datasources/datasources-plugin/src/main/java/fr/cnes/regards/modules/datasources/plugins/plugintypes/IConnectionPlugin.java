/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.plugintypes;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;

/**
 * Class IConnectionPlugin
 *
 * Allows to manage data sources
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@PluginInterface(description = "Plugin to connect to a data source")
public interface IConnectionPlugin {

    /**
     * Test the connection 
     * @return true if the connection is active
     */
    boolean testConnection();

}
