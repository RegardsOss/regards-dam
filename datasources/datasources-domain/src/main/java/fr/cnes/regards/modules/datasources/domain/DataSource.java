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

    private String label;

    // une table
    private String tableName;

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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String pLabel) {
        this.label = pLabel;
    }

}
