/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

import java.util.List;

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
    private String name_;

    /**
     * Model type
     */
    private ModelType type_;

    /**
     * Model attributes
     */
    private List<ModelAttribute> attributes_;

    /**
     * @return the name
     */
    public String getName() {
        return name_;
    }

    /**
     * @param pName
     *            the name to set
     */
    public void setName(String pName) {
        name_ = pName;
    }

    /**
     * @return the type
     */
    public ModelType getType() {
        return type_;
    }

    /**
     * @param pType
     *            the type to set
     */
    public void setType(ModelType pType) {
        type_ = pType;
    }

    /**
     * @return the attributes
     */
    public List<ModelAttribute> getAttributes() {
        return attributes_;
    }

    /**
     * @param pAttributes
     *            the attributes to set
     */
    public void setAttributes(List<ModelAttribute> pAttributes) {
        attributes_ = pAttributes;
    }
}
