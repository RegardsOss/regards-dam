/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.dataaccess.domain.accessright.validation.DataAccessRightValidation;
import fr.cnes.regards.modules.plugins.domain.PluginConfiguration;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
@DataAccessRightValidation
public class DataAccessRight {

    @Id
    @SequenceGenerator(name = "DataAccessRightSequence", initialValue = 1, sequenceName = "SEQ_DATA_ACCESS_RIGHT")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DataAccessRightSequence")
    private Long id;

    @NotNull
    @Enumerated
    private DataAccessLevel dataAccessLevel;

    @OneToOne
    private PluginConfiguration pluginConfiguration;

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
