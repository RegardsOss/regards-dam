/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.validation.constraints.NotNull;

import org.apache.poi.ss.formula.functions.T;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.jpa.utils.deserializer.LocalDateTimeDeserializer;
import fr.cnes.regards.framework.jpa.utils.serializer.LocalDateTimeSerializer;
import fr.cnes.regards.framework.jpa.validator.PastOrNow;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Base class for all entities(on a REGARDS point of view)
 *
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
public abstract class AbstractEntity implements IIdentifiable<Long> {

    /**
     * last time the entity was updated
     */
    @PastOrNow
    protected LocalDateTime lastUpdate;

    /**
     * time at which the entity was created
     */
    @PastOrNow
    protected LocalDateTime creationDate;

    /**
     * entity id for SGBD purpose mainly and REST request
     */
    protected Long id;

    /**
     * Information Package ID for REST request
     */
    protected String ipId;

    /**
     * Submission Information Package (SIP): which is the information sent from the producer to the archive used for
     * REST request
     */
    @NotNull
    protected final String sipId;

    /**
     * list of tags affected to this entity
     */
    protected List<String> tags;

    /**
     * list of attribute associated to this entity
     */
    protected List<IAttribute<T>> attributes;

    /**
     * model that this entity is respecting
     */
    protected Model model;

    public AbstractEntity() {
        creationDate = LocalDateTime.now();
        lastUpdate = LocalDateTime.now();
        sipId = String.format("%d-azrtyuiop", ThreadLocalRandom.current().nextInt(1, 1000000));
    }

    public AbstractEntity(Model pModel) {
        this();
        model = pModel;
    }

    /**
     * @return the lastUpdate
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @param pLastUpdate
     *            the lastUpdate to set
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public void setLastUpdate(LocalDateTime pLastUpdate) {
        lastUpdate = pLastUpdate;
    }

    /**
     * @return the creationDate
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * @param pCreationDate
     *            the creationDate to set
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public void setCreationDate(LocalDateTime pCreationDate) {
        creationDate = pCreationDate;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public String getIpId() {
        return ipId;
    }

    public void setIpId(String pIpId) {
        ipId = pIpId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> pTags) {
        tags = pTags;
    }

    public List<IAttribute<T>> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<IAttribute<T>> pAttributes) {
        attributes = pAttributes;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model pModel) {
        model = pModel;
    }

    public String getSipId() {
        return sipId;
    }

}
