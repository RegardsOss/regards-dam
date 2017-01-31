/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * @author Sylvain Vissiere-Guerinet
 * @author Marc Sordi
 */
@Entity
@Table(name = "T_DATA_SET")
public class DataSet extends AbstractLinkEntity {

    /**
     * Quality mark
     */
    @Column
    private int score;

    /**
     * this list contains IDs of plugin configuration for any plugin associated to this DataSet, for example: ids
     * configuration for Converters, Services, Filters
     */
    @OneToMany
    // this is a uni-directionnal OneToMany mapping so join column in on the Many side so it's the id of this
    // entity(DataSet)
    @JoinColumn(name = "data_set_id", foreignKey = @ForeignKey(name = "fk_dataset_datasource_id"))
    private List<PluginConfiguration> pluginConfigurationIds;

    /**
     * {@link DataSource} from which this DataSet presents data
     */
    @ManyToOne
    @JoinColumn(name = "datasource_id", foreignKey = @ForeignKey(name = "fk_dataset_datasource_id"))
    private DataSource dataSource;

    /**
     * request clause to subset a data from the DataSource, only used by the catalog(elasticsearch) as all data from
     * DataSource has been given to the catalog
     */
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private ICriterion subsettingClause;

    public DataSet(Model pModel, UniformResourceName pIpId, String pLabel) {
        super(pModel, pIpId, pLabel);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int pScore) {
        score = pScore;
    }

    @Override
    public String getType() {
        return EntityType.DATASET.toString();
    }

    public List<PluginConfiguration> getPluginConfigurationIds() {
        return pluginConfigurationIds;
    }

    public void setPluginConfigurationIds(List<PluginConfiguration> pPluginConfigurationIds) {
        pluginConfigurationIds = pPluginConfigurationIds;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource pDataSource) {
        dataSource = pDataSource;
    }

    public ICriterion getSubsettingClause() {
        return subsettingClause;
    }

    public void setSubsettingClause(ICriterion pSubsettingClause) {
        subsettingClause = pSubsettingClause;
    }

}
