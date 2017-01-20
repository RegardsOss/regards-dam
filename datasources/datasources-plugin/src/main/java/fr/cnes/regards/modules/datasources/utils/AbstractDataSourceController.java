/*
 * LICENSE_PLACEHOLDER
 */


package fr.cnes.regards.modules.datasources.utils;

import java.util.List;
import java.util.Map;


/**
 * This class provides the tables and the columns for a data source to an Oracle SGBD
 *
 * @author Christophe Mertz
 *
 */
public abstract class AbstractDataSourceController {

    private final String userName;

    private final String password;

    private final String url;

    /**
     * @param pUserName
     * @param pPassword
     * @param pUrl
     */
    public AbstractDataSourceController(String pUserName, String pPassword, String pUrl) {
        super();
        userName = pUserName;
        password = pPassword;
        url = pUrl;
    }

    protected abstract IDBDataSourceTableRepository getTableRepository() throws DataSourceUtilsException;

    protected abstract IDBDataSourceColumnRepository getColumnRepository() throws DataSourceUtilsException;

    /**
     * Return the list tables of the data source
     *
     * @return List<String>
     * @throws DataSourceUtilsException
     *
     * @since 1.0-SNAPSHOT
     */
    public List<String> getTables() throws DataSourceUtilsException {
        return getTableRepository().getTables();
    }

    /**
     * Get the columns and the SQL type for a specific table
     *
     * @param pTableName
     *            the database table to get the column
     *
     * @return the map of the columns and the SQL type
     * @throws DataSourceUtilsException
     *
     * @since 1.0-SNAPSHOT
     */
    public Map<String, String> getColumns(String pTableName) throws DataSourceUtilsException {
        return getColumnRepository().getColumns(pTableName);
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

}
