/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.domain;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 */
public class DataSource {

    /**
     * The associated {@link PluginConfiguration} to the {@link DataSource}
     */
    private Long pluginConfigurationId;

    /**
     * The associated {@link PluginConfiguration} to the {@link DBConnection}
     */
    private Long pluginConfigurationConnectionId;

    /**
     * The plugin class name that implements the IDBConnectionPlugin interface
     */
    private String pluginClassName;

    private Long modelId;

    // une table
    private String tableName;

    // ou les clauses from et where d'une requête

    private String whereClause;

    private String fromClause;

    private DataSourceModelMapping mapping;

    public Long getPluginConfigurationId() {
        return pluginConfigurationId;
    }

    public void setPluginConfigurationId(Long pluginConfigurationId) {
        this.pluginConfigurationId = pluginConfigurationId;
    }

    public Long getPluginConfigurationConnectionId() {
        return pluginConfigurationConnectionId;
    }

    public void setPluginConfigurationConnectionId(Long pluginConfigurationConnectionId) {
        this.pluginConfigurationConnectionId = pluginConfigurationConnectionId;
    }

    public String getPluginClassName() {
        return pluginClassName;
    }

    public void setPluginClassName(String pluginClassName) {
        this.pluginClassName = pluginClassName;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public String getFromClause() {
        return fromClause;
    }

    public void setFromClause(String fromClause) {
        this.fromClause = fromClause;
    }

    public DataSourceModelMapping getMapping() {
        return mapping;
    }

    public void setMapping(DataSourceModelMapping mapping) {
        this.mapping = mapping;
    }

}
