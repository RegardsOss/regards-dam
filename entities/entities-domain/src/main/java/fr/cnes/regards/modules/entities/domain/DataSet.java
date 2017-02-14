/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.annotations.Type;

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
@Table(name = "t_data_set")
public class DataSet extends AbstractLinkEntity {

    // TODO: add description
    /**
     * Quality mark
     */
    @Column
    @Min(0)
    @Max(10)
    private int score;

    /**
     * this list contains plugin configurations for any plugin associated to this DataSet, for example: configurations
     * for Converters, Services, Filters
     */
    // TODO handler for deletion events
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "t_dataset_plugin_configuration", joinColumns = @JoinColumn(name = "dataset_id"))
    private List<Long> pluginConfigurationIds;

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
    @Column(name = "sub_setting_clause", columnDefinition = "jsonb")
    private ICriterion subsettingClause;

    public DataSet() {
        super();
    }

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

    public List<Long> getPluginConfigurationIds() {
        return pluginConfigurationIds;
    }

    public void setPluginConfigurationIds(List<Long> pPluginConfigurationIds) {
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
