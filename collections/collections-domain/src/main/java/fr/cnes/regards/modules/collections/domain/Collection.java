/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.domain;

import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class Collection extends AbstractEntity {

    /**
     *
     */
    private String description;

    /**
     *
     */
    @NotNull
    private String name;

    public Collection() {
        super();
    }

    public Collection(Model pModel, String pDescription, String pName) {
        super(pModel);
        description = pDescription;
        name = pName;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param pDescription
     *            the description to set
     */
    public void setDescription(String pDescription) {
        description = pDescription;
    }

    /**
     * @param pName
     *            the name to set
     */
    public void setName(String pName) {
        name = pName;
    }

    @Override
    public boolean equals(Object pObj) {
        return (pObj instanceof Collection) && ((Collection) pObj).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
