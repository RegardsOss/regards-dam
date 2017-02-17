/*
 * LICENSE_PLACEHOLDER
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

    @NotNull
    @Column(length = 30, name = "data_access_level")
    @Enumerated(EnumType.STRING)
    private DataAccessLevel dataAccessLevel;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plugin_conf_id", foreignKey = @ForeignKey(name = "fk_access_right_plugin_conf"))
    private PluginConfiguration pluginConfiguration;

    protected DataAccessRight() {
    }

    public DataAccessRight(DataAccessLevel pDataAccessLevel) {
        dataAccessLevel = pDataAccessLevel;
    }

    public DataAccessRight(DataAccessLevel pDataAccessLevel, PluginConfiguration pPluginConf) { // NOSONAR
        this(pDataAccessLevel);
        pluginConfiguration = pPluginConf;
    }

    public DataAccessLevel getDataAccessLevel() {
        return dataAccessLevel;
    }

    public void setDataAccessLevel(DataAccessLevel pDataAccessLevel) {
        dataAccessLevel = pDataAccessLevel;
    }

    public PluginConfiguration getPluginConfiguration() {
        return pluginConfiguration;
    }

    public void setPluginConfiguration(PluginConfiguration pPluginConfiguration) {
        pluginConfiguration = pPluginConfiguration;
    }

}
