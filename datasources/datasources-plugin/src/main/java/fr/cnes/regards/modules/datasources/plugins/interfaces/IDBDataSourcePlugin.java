/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.interfaces;

import java.util.List;
import java.util.Map;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.datasources.utils.Column;
import fr.cnes.regards.modules.datasources.utils.Index;
import fr.cnes.regards.modules.datasources.utils.Table;

/**
 * Class IDBDataSourcePlugin
 * 
 * Allows to search in a data base, and to explore the database's tables, columns and indices
 *
 * @author Christophe Mertz
 * 
 */
@PluginInterface(description = "Plugin to explore and search in a data source")
public interface IDBDataSourcePlugin extends IDataSourcePlugin {

    /**
     * The model parameter name
     */
    public static final String MODEL_PARAM = "model";

    /**
     * The connection parameter name
     */
    public static final String CONNECTION_PARAM = "connection";
    
    public Map<String, Table> getTables();

    public Map<String, Column> getColumns(Table pTable);

    public Map<String, Index> getIndices(Table pTable);

    public void setMapping(String pTable, String... pColumns);
    
    public String getConfiguredTable();

    public List<String> getConfiguredColumns();

}
