/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import fr.cnes.regards.framework.jpa.utils.deserializer.LocalDateTimeDeserializer;
import fr.cnes.regards.framework.jpa.utils.serializer.LocalDateTimeSerializer;
import fr.cnes.regards.framework.jpa.validator.PastOrNow;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 *
 */
public abstract class Entity implements Identifiable<String> {

    @PastOrNow
    private LocalDateTime lastUpdate_;

    @PastOrNow
    private LocalDateTime creationDate_;

    /**
     * Submission Information Package (SIP): which is the information sent from the producer to the archive
     */
    @NotNull
    private String sid_id;

    private List<String> tags_;

    private List<IAttribute> attributes_;

    private Model model_;

    public Entity() {
        creationDate_ = LocalDateTime.now();
        lastUpdate_ = LocalDateTime.now();
        sid_id = String.format("%d-azrtyuiop", ThreadLocalRandom.current().nextInt(1, 1000000));
    }

    public Entity(Model pModel) {
        this();
        model_ = pModel;
    }

    /**
     * @return the lastUpdate
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getLastUpdate() {
        return lastUpdate_;
    }

    /**
     * @param pLastUpdate
     *            the lastUpdate to set
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public void setLastUpdate(LocalDateTime pLastUpdate) {
        lastUpdate_ = pLastUpdate;
    }

    /**
     * @return the creationDate
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getCreationDate() {
        return creationDate_;
    }

    /**
     * @param pCreationDate
     *            the creationDate to set
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public void setCreationDate(LocalDateTime pCreationDate) {
        creationDate_ = pCreationDate;
    }

    /**
     * @return the id of the submitted Package
     */
    public String getSidId() {
        return sid_id;
    }

    /**
     * @param pSid_id
     *            the sid_id to set
     */
    public void setSidId(String pSid_id) {
        sid_id = pSid_id;
    }

    /**
     * @return Return the entity id
     */
    @Override
    public String getId() {
        return sid_id;
    }

    /**
     * @param pId
     *            the id to set
     */
    public void setId(String pId) {
        sid_id = pId;
    }

    /**
     * @return the tags
     */
    public List<String> getTags() {
        return tags_;
    }

    /**
     * @param pTags
     *            the tags to set
     */
    public void setTags(List<String> pTags) {
        tags_ = pTags;
    }

    /**
     * @return the attributes
     */
    public List<IAttribute> getAttributes() {
        return attributes_;
    }

    /**
     * @param pAttributes
     *            the attributes to set
     */
    public void setAttributes(List<IAttribute> pAttributes) {
        attributes_ = pAttributes;
    }

    /**
     * @return the model
     */
    public Model getModel() {
        return model_;
    }

    /**
     * @param pModel
     *            the model to set
     */
    public void setModel(Model pModel) {
        model_ = pModel;
    }

}
