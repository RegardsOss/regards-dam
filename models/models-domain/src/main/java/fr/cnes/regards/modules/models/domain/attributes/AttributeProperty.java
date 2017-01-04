/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import fr.cnes.regards.framework.jpa.IIdentifiable;

/**
 *
 * Custom attribute property
 *
 * @author Marc Sordi
 *
 */
@Entity
@Table(name = "T_ATT_PPTY")
@SequenceGenerator(name = "attPropertySequence", initialValue = 1, sequenceName = "SEQ_ATT_PPTY")
public class AttributeProperty implements IIdentifiable<Long> {

    /**
     * Key max size
     */
    private static final int KEY_MAX_SIZE = 32;

    /**
     * Resource identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attPropertySequence")
    @Column(name = "id")
    private Long id;

    /**
     * Custom key
     */
    @NotNull
    @Size(max = KEY_MAX_SIZE)
    @Column(name = "pptkey", length = KEY_MAX_SIZE)
    private String key;

    /**
     * Custom value
     */
    @NotNull
    @Column(name = "pptvalue")
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String pKey) {
        this.key = pKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String pValue) {
        this.value = pValue;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }
}
