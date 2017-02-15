/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.interfaces;

import java.util.List;
import java.util.Map;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.datasources.utils.Column;
import fr.cnes.regards.modules.datasources.utils.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.utils.Index;
import fr.cnes.regards.modules.datasources.utils.Table;

/**
 * Class IDBDataSourcePlugin
 * 
 * Allows to search in a database, and to explore the database's tables, columns and indices.
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

    /**
     * Requests the database to get the {@link List} of database's table.
     * 
     * @return a {@link Map} of the database's table
     */
    public Map<String, Table> getTables();

    /**
     * Requests the database to get the columns of a specific table.
     * 
     * @param pTable
     *            the database's table to which teh colmns
     * @return a {@link Map} of the columns
     * 
     */
    public Map<String, Column> getColumns(Table pTable);

    public Map<String, Index> getIndices(Table pTable);

    /**
     * Allows to define the database table used, and the columns of this table.</br>
     * The tables and columns are used to generate the SQL request used to execute statement on the database.
     * 
     * @param pTable
     *            the name of the table
     * @param pMapping
     *            the mapping between the model and the datasource
     */
    public void setMapping(String pTable, DataSourceModelMapping pMapping);

    /**
     * The table of the database
     * 
     * @return the table name
     */
    public String getConfiguredTable();

    /**
     * Get the {@link List} of columns used
     * 
     * @return the {@link List} of columns name
     */
    public List<String> getConfiguredColumns();

}
