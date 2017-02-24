/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.interfaces;

import java.util.List;
import java.util.Map;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.Index;
import fr.cnes.regards.modules.datasources.domain.Table;

/**
 * Class IDataSourceFromSingleTablePlugin
 * 
 * Allows to search in a database, and to explore the database's tables, columns and indexes.
 *
 * @author Christophe Mertz
 * 
 */
@PluginInterface(description = "Plugin to explore a data source and search in a single table of the data source")
public interface IDataSourceFromSingleTablePlugin extends IDataSourcePlugin {

    /**
     * The table parameter name
     */
    public static final String TABLE_PARAM = "table";



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

}
