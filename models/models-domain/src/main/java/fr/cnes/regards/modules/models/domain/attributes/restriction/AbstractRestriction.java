/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;

import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.models.domain.adapters.gson.RestrictionJsonAdapterFactory;

/**
 * @author msordi
 *
 */
@Entity(name = "T_RESTRICTION")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@SequenceGenerator(name = "restrictionSequence", initialValue = 1, sequenceName = "SEQ_RESTRICTION")
@JsonAdapter(RestrictionJsonAdapterFactory.class)
public abstract class AbstractRestriction implements IRestriction, IIdentifiable<Long> {

    /**
     * Internal identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "restrictionSequence")
    private Long id;

    /**
     * Attribute restriction type
     */
    @Enumerated(EnumType.STRING)
    public RestrictionType type;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    @Override
    public RestrictionType getType() {
        return type;
    }

    public void setType(RestrictionType pType) {
        type = pType;
    }
}
