/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.domain;

import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.entities.domain.Entity;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 *
 */
public class Collection extends Entity {

    private String description_;

    @NotNull
    private String name_;

    /**
     * @param pSid_id
     * @param pModel
     */
    public Collection() {
        super();
    }

    /**
     *
     * @param pSid_id
     * @param pDescription
     * @param pName
     */
    public Collection(Model pModel, String pDescription, String pName) {
        super(pModel);
        description_ = pDescription;
        name_ = pName;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description_;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name_;
    }

    /**
     * @param pDescription
     *            the description to set
     */
    public void setDescription(String pDescription) {
        description_ = pDescription;
    }

    /**
     * @param pName
     *            the name to set
     */
    public void setName(String pName) {
        name_ = pName;
    }

    @Override
    public boolean equals(Object pObj) {
        return (pObj instanceof Collection) && ((Collection) pObj).getId().equals(getId());
    }
}
