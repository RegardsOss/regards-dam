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
package fr.cnes.regards.modules.entities.rest.dto;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Same as a {@link Dataset}, with the subsettingClause as a String (for OpenSearch query).
 *
 * @author Xavier-Alexandre Brochard
 */
public class DatasetDto implements IIdentifiable<Long>, IIndexable {

    private int score;

    private PluginConfiguration plgConfDataSource;

    private Long dataModel;

    private String subsettingClause;

    private Set<String> quotations = new HashSet<>();

    private String licence;

    private DescriptionFile descriptionFile;

    private final String entityType = EntityType.DATASET.toString();

    protected UniformResourceName ipId;

    protected String label;

    protected Model model;

    protected OffsetDateTime lastUpdate;

    protected OffsetDateTime creationDate;

    protected Long id;

    protected String sipId;

    protected Set<String> tags = new HashSet<>();

    protected Set<String> groups = new HashSet<>();

    // protected Set<AbstractAttribute<?>> properties = new HashSet<>();
    protected Object properties = new Object();

    protected IGeometry geometry;

    /**
     * Default constructor
     */
    public DatasetDto() {
        this(null, null, null);
    }

    /**
     * Constructor
     * @param pModel
     * @param pTenant
     * @param pLabel
     */
    public DatasetDto(Model pModel, String pTenant, String pLabel) {
        model = pModel;
        ipId = new UniformResourceName(OAISIdentifier.AIP, EntityType.DATASET, pTenant, UUID.randomUUID(), 1);
        label = pLabel;
    }

    /**
     * @return the score
     */
    public int getScore() {
        return score;
    }

    /**
     * @param pScore the score to set
     */
    public void setScore(int pScore) {
        score = pScore;
    }

    /**
     * @return the plgConfDataSource
     */
    public PluginConfiguration getPlgConfDataSource() {
        return plgConfDataSource;
    }

    /**
     * @param pPlgConfDataSource the plgConfDataSource to set
     */
    public void setPlgConfDataSource(PluginConfiguration pPlgConfDataSource) {
        plgConfDataSource = pPlgConfDataSource;
    }

    /**
     * @return the dataModel
     */
    public Long getDataModel() {
        return dataModel;
    }

    /**
     * @param pDataModel the dataModel to set
     */
    public void setDataModel(Long pDataModel) {
        dataModel = pDataModel;
    }

    /**
     * @return the subsettingClause
     */
    public String getSubsettingClause() {
        return subsettingClause;
    }

    /**
     * @param pSubsettingClause the subsettingClause to set
     */
    public void setSubsettingClause(String pSubsettingClause) {
        subsettingClause = pSubsettingClause;
    }

    /**
     * @return the quotations
     */
    public Set<String> getQuotations() {
        return quotations;
    }

    /**
     * @param pQuotations the quotations to set
     */
    public void setQuotations(Set<String> pQuotations) {
        quotations = pQuotations;
    }

    /**
     * @return the licence
     */
    public String getLicence() {
        return licence;
    }

    /**
     * @param pLicence the licence to set
     */
    public void setLicence(String pLicence) {
        licence = pLicence;
    }

    /**
     * @return the descriptionFile
     */
    public DescriptionFile getDescriptionFile() {
        return descriptionFile;
    }

    /**
     * @param pDescriptionFile the descriptionFile to set
     */
    public void setDescriptionFile(DescriptionFile pDescriptionFile) {
        descriptionFile = pDescriptionFile;
    }

    /**
     * @return the ipId
     */
    public UniformResourceName getIpId() {
        return ipId;
    }

    /**
     * @param pIpId the ipId to set
     */
    public void setIpId(UniformResourceName pIpId) {
        ipId = pIpId;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param pLabel the label to set
     */
    public void setLabel(String pLabel) {
        label = pLabel;
    }

    /**
     * @return the model
     */
    public Model getModel() {
        return model;
    }

    /**
     * @param pModel the model to set
     */
    public void setModel(Model pModel) {
        model = pModel;
    }

    /**
     * @return the lastUpdate
     */
    public OffsetDateTime getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @param pLastUpdate the lastUpdate to set
     */
    public void setLastUpdate(OffsetDateTime pLastUpdate) {
        lastUpdate = pLastUpdate;
    }

    /**
     * @return the creationDate
     */
    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * @param pCreationDate the creationDate to set
     */
    public void setCreationDate(OffsetDateTime pCreationDate) {
        creationDate = pCreationDate;
    }

    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param pId the id to set
     */
    public void setId(Long pId) {
        id = pId;
    }

    /**
     * @return the sipId
     */
    public String getSipId() {
        return sipId;
    }

    /**
     * @param pSipId the sipId to set
     */
    public void setSipId(String pSipId) {
        sipId = pSipId;
    }

    /**
     * @return the tags
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * @param pTags the tags to set
     */
    public void setTags(Set<String> pTags) {
        tags = pTags;
    }

    /**
     * @return the groups
     */
    public Set<String> getGroups() {
        return groups;
    }

    /**
     * @param pGroups the groups to set
     */
    public void setGroups(Set<String> pGroups) {
        groups = pGroups;
    }

    /**
     * @return the properties
     */
    public Object getProperties() {
        return properties;
    }

    /**
     * @param pProperties the properties to set
     */
    public void setProperties(Set<AbstractAttribute<?>> pProperties) {
        properties = pProperties;
    }

    /**
     * @return the geometry
     */
    public IGeometry getGeometry() {
        return geometry;
    }

    /**
     * @param pGeometry the geometry to set
     */
    public void setGeometry(IGeometry pGeometry) {
        geometry = pGeometry;
    }

    @Override
    public String getDocId() {
        return ipId.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.indexer.domain.IIndexable#getType()
     */
    @Override
    public String getType() {
        return entityType;
    }

}