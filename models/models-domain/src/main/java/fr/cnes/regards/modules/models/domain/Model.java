/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * Define a model
 *
 * @author msordi
 *
 */
public class Model {

    /**
     * Model name
     */
    private String name;

    /**
     * Optional attribute description
     */
    private Optional<String> description;

    /**
     * Model type
     */
    private ModelType type;

    /**
     * Model attributes
     */
    private SortedSet<ModelAttribute> attributes;

    public Model() {
        attributes = new TreeSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public ModelType getType() {
        return type;
    }

    public void setType(ModelType pType) {
        type = pType;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public void setDescription(Optional<String> pDescription) {
        description = pDescription;
    }

    public SortedSet<ModelAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(SortedSet<ModelAttribute> pAttributes) {
        attributes = pAttributes;
    }

    public void addAttribute(ModelAttribute pAttribute) {

    }
}
