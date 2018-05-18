/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.dataaccess.domain.accessright.validation.DataAccessRightValidation;

/**
 * Only matters if the {@link AccessLevel} to the dataset is {@link AccessLevel#FULL_ACCESS}
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Embeddable
@DataAccessRightValidation
public class DataAccessRight {

    /**
     * Data access level
     */
    @NotNull
    @Column(length = 30, name = "data_access_level")
    @Enumerated(EnumType.STRING)
    private DataAccessLevel dataAccessLevel;

    /**
     * Plugin configuration allowing to customize the data access level
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plugin_conf_id", foreignKey = @ForeignKey(name = "fk_access_right_plugin_conf"))
    private PluginConfiguration pluginConfiguration;

    /**
     * Default constructor
     */
    protected DataAccessRight() {
    }

    /**
     * Constructor setting the parameter as attribute
     * @param pDataAccessLevel
     */
    public DataAccessRight(DataAccessLevel pDataAccessLevel) {
        dataAccessLevel = pDataAccessLevel;
    }

    /**
     * Constructor setting the parameters as attribute
     * @param pDataAccessLevel
     * @param pPluginConf
     */
    public DataAccessRight(DataAccessLevel pDataAccessLevel, PluginConfiguration pPluginConf) { // NOSONAR
        this(pDataAccessLevel);
        pluginConfiguration = pPluginConf;
    }

    /**
     * @return the data access level
     */
    public DataAccessLevel getDataAccessLevel() {
        return dataAccessLevel;
    }

    /**
     * Set the data access level
     * @param pDataAccessLevel
     */
    public void setDataAccessLevel(DataAccessLevel pDataAccessLevel) {
        dataAccessLevel = pDataAccessLevel;
    }

    /**
     * @return the plugin configuration
     */
    public PluginConfiguration getPluginConfiguration() {
        return pluginConfiguration;
    }

    /**
     * Set the plugin
     * @param pPluginConfiguration
     */
    public void setPluginConfiguration(PluginConfiguration pPluginConfiguration) {
        pluginConfiguration = pPluginConfiguration;
    }

}
