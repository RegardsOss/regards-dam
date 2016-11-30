/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.dto;

import fr.cnes.regards.modules.models.domain.ComputationMode;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;

/**
 *
 * DTO merged of {@link AttributeModel} and {@link ModelAttribute}
 *
 * @author Marc Sordi
 *
 */
public class AttributeDto {

    /**
     * Attribute name
     */
    private String name;

    /**
     * Optional attribute description
     */
    private String description;

    /**
     * Attribute type
     */
    private AttributeType type;

    /**
     * Whether this attribute is a search criterion
     */
    private boolean queryable;

    /**
     * Whether this attribute can be used for facet<br/>
     * Only queryable attribute can be a facet!
     */
    private boolean facetable;

    /**
     * Whether this attribute can be alterate by users
     */
    private boolean alterable;

    /**
     * Whether this attribute is optional
     */
    private boolean optional;

    /**
     * Calculation mode
     */
    private ComputationMode mode;

    /**
     * Applicable restriction
     */
    private AbstractRestriction restriction;

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType pType) {
        type = pType;
    }

    public boolean isQueryable() {
        return queryable;
    }

    public void setQueryable(boolean pQueryable) {
        queryable = pQueryable;
    }

    public boolean isFacetable() {
        return facetable;
    }

    public void setFacetable(boolean pFacetable) {
        facetable = pFacetable;
    }

    public boolean isAlterable() {
        return alterable;
    }

    public void setAlterable(boolean pAlterable) {
        alterable = pAlterable;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean pOptional) {
        optional = pOptional;
    }

    public AbstractRestriction getRestriction() {
        return restriction;
    }

    public void setRestriction(AbstractRestriction pRestriction) {
        restriction = pRestriction;
    }

    public ComputationMode getMode() {
        return mode;
    }

    public void setMode(ComputationMode pMode) {
        mode = pMode;
    }
}
