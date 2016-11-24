/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import fr.cnes.regards.framework.jpa.IIdentifiable;

/**
 *
 * Define a model
 *
 * @author msordi
 *
 */
@Entity
@Table(name = "T_MODEL", indexes = { @Index(name = "IDX_MODEL_NAME", columnList = "name") })
@SequenceGenerator(name = "modelSequence", initialValue = 1, sequenceName = "SEQ_MODEL")
public class Model implements IIdentifiable<Long> {

    /**
     * Name regular expression
     */
    private static final String MODEL_NAME_REGEXP = "[0-9a-zA-Z_]*";

    /**
     * Name min size
     */
    private static final int MODEL_NAME_MIN_SIZE = 3;

    /**
     * Name max size
     */
    private static final int MODEL_NAME_MAX_SIZE = 32;

    /**
     * Internal identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "modelSequence")
    private Long id;

    /**
     * Model name
     */
    @NotNull
    @Pattern(regexp = MODEL_NAME_REGEXP, message = "Model name must conform to regular expression \""
            + MODEL_NAME_REGEXP + "\".")
    @Size(min = MODEL_NAME_MIN_SIZE, max = MODEL_NAME_MAX_SIZE, message = "Attribute name must be between "
            + MODEL_NAME_MIN_SIZE + " and " + MODEL_NAME_MAX_SIZE + " length.")
    @Column(nullable = false, updatable = false, unique = true)
    private String name;

    /**
     * Optional attribute description
     */
    private String description;

    /**
     * Model type
     */
    @NotNull
    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ModelType type;

    public static Model build(String pName, String pDescription, ModelType pModelType) {
        final Model model = new Model();
        model.setName(pName);
        model.setDescription(pDescription);
        model.setType(pModelType);
        return model;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }
}
