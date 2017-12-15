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

import javax.validation.constraints.NotNull;

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
    @NotNull
    private Long pluginConfigurationConnectionId;

    /**
     * The plugin class name that implements the IDataSourcePlugin interface
     */
    @NotNull
    private String pluginClassName;

    @NotNull
    private String label;

    private String tableName;

    private String fromClause;

    private DataSourceModelMapping mapping;

    /**
     * The refresh rate
     */
    private Integer refreshRate;

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

    /**
     * Set the refresh rate
     * @param refreshRate
     */
    public void setRefreshRate(Integer refreshRate) {
        this.refreshRate = refreshRate;
    }

    /**
     * @return the refresh rate
     */
    public Integer getRefreshRate() {
        return refreshRate;
    }
}
