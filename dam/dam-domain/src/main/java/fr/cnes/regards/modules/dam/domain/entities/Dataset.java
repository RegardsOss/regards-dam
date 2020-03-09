/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.domain.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.dam.domain.entities.feature.DatasetFeature;
import fr.cnes.regards.modules.dam.domain.entities.metadata.DatasetMetadata;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

/**
 * Dataset feature decorator
 *
 * @author Sylvain Vissiere-Guerinet
 * @author Marc Sordi
 * @author Christophe Mertz
 * @author oroussel
 */
@Entity
@DiscriminatorValue("DATASET")
public class Dataset extends AbstractEntity<DatasetFeature> {

    public static final String DATA_SOURCE_ID = "dataSourceId";

    public static final String LAST_UPDATE = "lastUpdate";

    /**
     * A PluginConfiguration for a plugin type IDataSourcePlugin.</br>
     * This PluginConfiguration defined the DataSource from which this Dataset presents data. <b>nullable = true</b> is
     * necessary because of single-table entity mapping (same table is used for all types of entities and other haven't
     * plugin configuration).
     */
    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "ds_plugin_conf_id", foreignKey = @ForeignKey(name = "fk_ds_plugin_conf_id"), nullable = true,
            updatable = false)
    private PluginConfiguration plgConfDataSource;

    /**
     * Model name of the Data objects held by this Dataset.
     * nullable=true because all abstract entities share the same table (single table mapping)
     */
    @Column(name = "data_model_name", updatable = false, nullable = true)
    private String dataModel;

    /**
     * Request clause to subset data from the DataSource, only used by the catalog(elasticsearch) as all data from
     * DataSource has been given to the catalog
     */
    @Type(type = "jsonb")
    @Column(name = "sub_setting_clause", columnDefinition = "jsonb")
    private ICriterion subsettingClause;

    /**
     * Representation of the above subsetting clause as an OpenSearch string request
     */
    @Type(type = "text")
    @Column(name = "sub_setting_clause_as_string")
    private String openSearchSubsettingClause;

    /**
     * Metadata, only used by Elasticsearch
     */
    @Transient
    private DatasetMetadata metadata = new DatasetMetadata();

    public Dataset() {
        // we use super and not this because at deserialization we need a ipId null at the object creation which is then
        // replaced by the attribute if present or added by creation method
        super(null, null);
    }

    public Dataset(Model model, String tenant, String providerId, String label) {
        super(model, new DatasetFeature(tenant, providerId, label));
    }

    /**
     * Get the ICriterion tree containing DATA_SOURCE_ID restriction ie different from saved subsetting clause and
     * opensearch one which are user-friendly (do not contain views of intern structure)
     * @return  {@link ICriterion}
     */
    public ICriterion getSubsettingClause() {
        ICriterion subsettingCrit = subsettingClause;
        // Add datasource id restriction
        if ((subsettingCrit == null) || (subsettingCrit == ICriterion.all())) {
            subsettingCrit = ICriterion.eq(DATA_SOURCE_ID, plgConfDataSource.getId());
        } else {
            subsettingCrit = ICriterion.and(subsettingCrit, ICriterion.eq(DATA_SOURCE_ID, plgConfDataSource.getId()));
        }
        return subsettingCrit;
    }

    /**
     * Set the subsetting clause
     * @param subsettingClause
     */
    public void setSubsettingClause(ICriterion subsettingClause) {
        this.subsettingClause = subsettingClause;
    }

    public ICriterion getUserSubsettingClause() {
        return (subsettingClause == null) ? ICriterion.all() : subsettingClause;
    }

    public PluginConfiguration getDataSource() {
        return plgConfDataSource;
    }

    public void setDataSource(PluginConfiguration plgConfDataSource) {
        this.plgConfDataSource = plgConfDataSource;
    }

    /**
     * @return the data model
     */
    public String getDataModel() {
        return dataModel;
    }

    /**
     * Set the data model
     * @param dataModel
     */
    public void setDataModel(String dataModel) {
        this.dataModel = dataModel;
    }

    public DatasetMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DatasetMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object pObj) {
        return super.equals(pObj);
    }

    /**
     * @return the openSearchSubsettingClause
     */
    public String getOpenSearchSubsettingClause() {
        return openSearchSubsettingClause;
    }

    /**
     * @param openSearchSubsettingClause the openSearchSubsettingClause to set
     */
    public void setOpenSearchSubsettingClause(String openSearchSubsettingClause) {
        this.openSearchSubsettingClause = openSearchSubsettingClause;
    }

    public String getLicence() {
        return feature.getLicence();
    }

    public void setLicence(String licence) {
        feature.setLicence(licence);
    }
}
