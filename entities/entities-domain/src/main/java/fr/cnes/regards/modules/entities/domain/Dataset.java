/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * @author Sylvain Vissiere-Guerinet
 * @author Marc Sordi
 * @author Christophe Mertz
 */
@Entity
public class Dataset extends AbstractLinkEntity {

    /**
     * Quality mark
     */
    @Column
    @Min(0)
    @Max(10)
    private int score;

    /**
     * this list contains plugin configurations for any plugin associated to this Dataset, for example: configurations
     * for Converters, Services, Filters
     */
    // TODO handler for deletion events
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "t_dataset_plugin_configuration", joinColumns = @JoinColumn(name = "dataset_id"))
    private List<Long> pluginConfigurationIds = new ArrayList<>();

    /**
     * A PluginConfiguration for a plugin type IDataSourcePlugin.</br>
     * This PluginConfiguration defined the DataSource from which this Dataset presents data.
     */
    @ManyToOne
    @JoinColumn(name = "plgconf_id", foreignKey = @ForeignKey(name = "fk_pluginconf_id"), nullable = true,
            updatable = true)
    private PluginConfiguration plgConfDataSource;

    @ManyToOne
    @JoinColumn(name = "model_data_id", foreignKey = @ForeignKey(name = "fk_model_id"), nullable = true,
            updatable = true)
    private Model modelOfData;

    /**
     * Request clause to subset data from the DataSource, only used by the catalog(elasticsearch) as all data from
     * DataSource has been given to the catalog
     */
    @Type(type = "jsonb")
    @Column(name = "sub_setting_clause", columnDefinition = "jsonb")
    private ICriterion subsettingClause;

    /**
     * set of quotations associated to the {@link Dataset}
     */
    @ElementCollection
    @CollectionTable(name = "t_dataset_quotation", joinColumns = @JoinColumn(name = "dataset_id"))
    private Set<String> quotations = new HashSet<>();

    /**
     * Dataset licence
     */
    @Type(type = "text")
    @Column
    @NotNull
    private String licence;

    public Dataset() {
        this(null, null, null);
    }

    public Dataset(Model pModel, String pTenant, String pLabel) {
        super(pModel, new UniformResourceName(OAISIdentifier.AIP, EntityType.DATASET, pTenant, UUID.randomUUID(), 1),
              pLabel);
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

    public ICriterion getSubsettingClause() {
        return subsettingClause;
    }

    public PluginConfiguration getDataSource() {
        return plgConfDataSource;
    }

    public void setDataSource(PluginConfiguration plgConfDataSource) {
        this.plgConfDataSource = plgConfDataSource;
    }

    public void setSubsettingClause(ICriterion pSubsettingClause) {
        subsettingClause = pSubsettingClause;
    }

    public Model getModelOfData() {
        return modelOfData;
    }

    public void setModelOfData(Model modelOfData) {
        this.modelOfData = modelOfData;
    }

    public Set<String> getQuotations() {
        return quotations;
    }

    public void setQuotations(Set<String> pQuotations) {
        quotations = pQuotations;
    }

    public void addQuotation(String pQuotations) {
        quotations.add(pQuotations);
    }

    public String getLicence() {
        return licence;
    }

    public void setLicence(String pLicence) {
        licence = pLicence;
    }

}
