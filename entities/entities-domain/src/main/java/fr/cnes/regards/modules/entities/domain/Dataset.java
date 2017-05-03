/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * @author Sylvain Vissiere-Guerinet
 * @author Marc Sordi
 * @author Christophe Mertz
 * @author oroussel
 */
@Entity
@DiscriminatorValue("DATASET")
public class Dataset extends AbstractDescEntity {

    /**
     * value allowing the system to order a set of result
     */
    @Column
    private int score;

    /**
     * A PluginConfiguration for a plugin type IDataSourcePlugin.</br>
     * This PluginConfiguration defined the DataSource from which this Dataset presents data. <b>nullable = true</b> is
     * necessary because of single-table entity mapping (same table is used for all types of entities and other haven't
     * plugin configuration).
     */
    @ManyToOne
    @JoinColumn(name = "ds_plugin_conf_id", foreignKey = @ForeignKey(name = "fk_ds_plugin_conf_id"), nullable = true,
            updatable = false)
    private PluginConfiguration plgConfDataSource;

    /**
     * Model id of the Data objects held by this Dataset. nullable=true because fo single table
     */
    @Column(name = "data_model_id", updatable = false, nullable = true)
    private Long dataModel;

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

    public ICriterion getSubsettingClause() {
        return subsettingClause;
    }

    public PluginConfiguration getDataSource() {
        return plgConfDataSource;
    }

    public void setDataSource(PluginConfiguration pPlgConfDataSource) {
        plgConfDataSource = pPlgConfDataSource;
    }

    public void setSubsettingClause(ICriterion pSubsettingClause) {
        subsettingClause = pSubsettingClause;
    }

    public Long getDataModel() {
        return dataModel;
    }

    public void setDataModel(Long pDataModel) {
        dataModel = pDataModel;
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

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object pObj) {
        return super.equals(pObj);
    }
}
