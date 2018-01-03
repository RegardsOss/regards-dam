/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.datasources.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;

/**
 * @author Christophe Mertz
 * @deprecated Directly use PluginConfiguration instead of this class
 */
@Deprecated
public class DBConnection {

    /**
     * The associated {@link PluginConfiguration}
     */
    private Long pluginConfigurationId;

    /**
     * The label of the DbConnection
     */
    @NotBlank
    private String label;

    /**
     * The plugin class name that implements the IDBConnectionPlugin interface
     */
    @NotBlank
    private String pluginClassName;

    /**
     * The user to used for the database connection
     */
    @NotBlank
    private String user;

    /**
     * The user's password to used for the database connection
     */
    @NotBlank
    private String password;

    /**
     * The URL to the database's host
     */
    @NotBlank
    private String dbHost;

    /**
     * The PORT to the database's host
     */
    @NotBlank
    private String dbPort;

    /**
     * The NAME of the database
     */
    @NotBlank
    private String dbName;

    /**
     * Maximum number of Connections a pool will maintain at any given time.
     */
    @NotNull
    private Integer maxPoolSize = 10;

    /**
     * Minimum number of Connections a pool will maintain at any given time.
     */
    @NotNull
    @Min(3)
    private Integer minPoolSize = 3;

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

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public String getDbPort() {
        return dbPort;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
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
