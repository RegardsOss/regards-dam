/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.dto;

import java.util.List;

import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 * {@link Fragment} DTO
 *
 * @author Marc Sordi
 *
 */
public class FragmentDto {

    /**
     * Fragment name
     */
    private String name;

    /**
     * Fragment description
     */
    private String description;

    /**
     * List of attributes
     */
    private List<AttributeDto> attributes;

    public FragmentDto() {
        super();
        // setDefType(DefinitionType.FRAGMENT);
    }

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

    public List<AttributeDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDto> pAttributes) {
        attributes = pAttributes;
    }

}
