/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.interfaces;

import java.util.List;
import java.util.Map;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.datasources.plugins.domain.Column;
import fr.cnes.regards.modules.datasources.plugins.domain.Index;
import fr.cnes.regards.modules.datasources.plugins.domain.Table;

/**
 * Class IDBDataSourcePlugin
 * 
 * Allows to search in a data base, and to explore the database's tables, columns and indexes
 *
 * @author Christophe Mertz
 * 
 */
@PluginInterface(description = "Plugin to explore and search in a data source")
public interface IDBDataSourcePlugin extends IDataSourcePlugin {

    public Map<String, Table> getTables();

    public Map<String, Column> getColumns(Table pTable);

    public Map<String, Index> getIndexes(Table pTable);

    public Table getConfiguredTable();

    public List<Column> getConfiguredColumns();

    // public boolean isRepositoryInit();

}
