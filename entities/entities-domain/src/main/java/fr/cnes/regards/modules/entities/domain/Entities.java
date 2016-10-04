/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.Identifiable;

import fr.cnes.regards.modules.core.validation.PastOrNow;

/**
 * @author lmieulet
 *
 */
public class Entities implements Identifiable<Long> {

    @NotNull
    private Long id_;

    @PastOrNow
    private LocalDateTime lastUpdate_;

    @PastOrNow
    private LocalDateTime creationDate_;

    /**
     * Submission Information Package (SIP): which is the information sent from the producer to the archive
     */
    @NotNull
    private String sid_id_;

    public Entities() {
        super();
        id_ = (long) ThreadLocalRandom.current().nextInt(1, 1000000);
        creationDate_ = LocalDateTime.now();
        lastUpdate_ = LocalDateTime.now();
    }

    /**
     * @param sid_id
     *            The id of the submitted Package
     */
    public Entities(String pSid_id) {
        this();
        sid_id_ = pSid_id;
    }

    /**
     * @return Return the entity id
     */
    @Override
    public Long getId() {
        return id_;
    }

    /**
     * @param pId
     *            the id to set
     */
    public void setId(Long pId) {
        id_ = pId;
    }

    /**
     * @return the lastUpdate
     */
    public LocalDateTime getLastUpdate() {
        return lastUpdate_;
    }

    /**
     * @param pLastUpdate
     *            the lastUpdate to set
     */
    public void setLastUpdate(LocalDateTime pLastUpdate) {
        lastUpdate_ = pLastUpdate;
    }

    /**
     * @return the creationDate
     */
    public LocalDateTime getCreationDate() {
        return creationDate_;
    }

    /**
     * @param pCreationDate
     *            the creationDate to set
     */
    public void setCreationDate(LocalDateTime pCreationDate) {
        creationDate_ = pCreationDate;
    }

    /**
     * @return the id of the submitted Package
     */
    public String getSidId() {
        return sid_id_;
    }

    /**
     * @param pSid_id
     *            the sid_id to set
     */
    public void setSidId(String pSid_id) {
        sid_id_ = pSid_id;
    }

}
