/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.plugintypes;

import java.util.List;
import java.util.Map;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.datasources.plugins.domain.Column;
import fr.cnes.regards.modules.datasources.plugins.domain.Table;

/**
 * Class IDBDataSourcePlugin
 * 
 * Allows to search in a data base, and to explore a
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@PluginInterface(description = "Plugin to explore and search in a data source")
public interface IDBDataSourcePlugin extends IDataSourcePlugin {

    public List<String> getTables();

    public Map<String, String> getColumns(String pTableName);

    public Table getConfiguredTable();

    public List<Column> getConfiguredColumns();

    public boolean isRepositoryInit();

}
