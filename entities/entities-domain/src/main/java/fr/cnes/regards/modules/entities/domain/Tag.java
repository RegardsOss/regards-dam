/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
@Table(name = "T_TAG")
public class Tag {

    @Id
    @SequenceGenerator(name = "TagSequence", initialValue = 1, sequenceName = "SEQ_TAG")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TagSequence")
    private Long id;

    @NotNull
    private String value;

    public Tag(String pValue) {
        value = pValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String pValue) {
        value = pValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    @Override
    public boolean equals(Object pOther) {
        return (pOther instanceof Tag) && ((Tag) pOther).value.equals(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
