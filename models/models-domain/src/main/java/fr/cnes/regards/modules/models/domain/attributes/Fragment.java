/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes;

import java.util.Optional;

import javax.validation.constraints.NotNull;

/**
 * @author msordi
 *
 */
public class Fragment {

    /**
     * Attribute name
     */
    @NotNull
    private String name;

    /**
     * Optional attribute description
     */
    private Optional<String> description;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param pName
     *            the name to set
     */
    public void setName(String pName) {
        name = pName;
    }

    /**
     * @return the description
     */
    public Optional<String> getDescription() {
        return description;
    }

    /**
     * @param pDescription
     *            the description to set
     */
    public void setDescription(Optional<String> pDescription) {
        description = pDescription;
    }
}
