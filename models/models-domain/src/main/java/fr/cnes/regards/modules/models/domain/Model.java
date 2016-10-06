/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.hateoas.Identifiable;

/**
 *
 * Define a model
 *
 * @author msordi
 *
 */
public class Model implements Identifiable<Long> {

    private final Long id_;

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

    public Model() {
        super();
        id_ = (long) ThreadLocalRandom.current().nextInt(1, 1000000);
    }

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

    /**
     *
     * @return model id
     */
    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return id_;
    }
}
