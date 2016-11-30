/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.dto;

import java.util.List;

import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelType;

/**
 *
 * {@link Model} DTO
 *
 * @author Marc Sordi
 *
 */
public class ModelDto {

    /**
     * Fragment name
     */
    private String name;

    /**
     * Fragment description
     */
    private String description;

    /**
     * Model type
     */
    private ModelType type;

    /**
     * List of attributes
     */
    private List<AttributeDto> attributes;

    /**
     * List of fragments
     */
    private List<FragmentDto> fragments;

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

    public ModelType getType() {
        return type;
    }

    public void setType(ModelType pType) {
        type = pType;
    }

    public List<FragmentDto> getFragments() {
        return fragments;
    }

    public void setFragments(List<FragmentDto> pFragments) {
        fragments = pFragments;
    }
}
