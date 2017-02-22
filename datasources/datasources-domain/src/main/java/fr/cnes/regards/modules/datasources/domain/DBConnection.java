/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.domain;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;

/**
 * @author Christophe Mertz
 *
 */
public class DBConnection {

    /**
     * The associated {@link PluginConfiguration}
     */
    private Long pluginConfigurationId;

    /**
     * The label of the DbConnection
     */
    private String label;

    /**
     * The plugin class name that implements the IDBConnectionPlugin interface
     */
    private String pluginClassName;

    /**
     * The user to used for the database connection
     */
    private String user;

    /**
     * The user's password to used for the database connection
     */
    private String password;

    /**
     * The URL_PARAM of the database
     */
    private String url;

    /**
     * Maximum number of Connections a pool will maintain at any given time.
     */
    private Integer maxPoolSize;

    /**
     * Minimum number of Connections a pool will maintain at any given time.
     */
    private Integer minPoolSize;

    public Long getPluginConfigurationId() {
        return pluginConfigurationId;
    }

    public void setPluginConfigurationId(Long pluginConfigurationId) {
        this.pluginConfigurationId = pluginConfigurationId;
    }

    public String getPluginClassName() {
        return pluginClassName;
    }

    public void setPluginClassName(String pluginClassName) {
        this.pluginClassName = pluginClassName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public Integer getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(Integer minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
