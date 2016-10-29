/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import fr.cnes.regards.framework.jpa.IIdentifiable;

/**
 *
 * Define a model
 *
 * @author msordi
 *
 */
@Entity
@Table(name = "T_MODEL")
@SequenceGenerator(name = "modelSequence", initialValue = 1, sequenceName = "SEQ_MODEL")
public class Model implements IIdentifiable<Long> {

    /**
     * Internal identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "modelSequence")
    private Long id;

    /**
     * Model name
     */
    private String name;

    /**
     * Optional attribute description
     */
    private String description;

    /**
     * Model type
     */
    private ModelType type;

    /**
     * Model attributes
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pos ASC")
    private SortedSet<ModelAttribute> attributes;

    public Model() {
        attributes = new TreeSet<>();
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
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
        return Optional.ofNullable(description);
    }

    public void setDescription(String pDescription) {
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
