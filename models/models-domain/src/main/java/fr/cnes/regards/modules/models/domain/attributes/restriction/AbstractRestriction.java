/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.models.domain.adapters.gson.RestrictionJsonAdapterFactory;
import fr.cnes.regards.modules.models.schema.Restriction;

/**
 * @author msordi
 *
 */
@Entity
@Table(name = "t_restriction")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", length = 16)
@SequenceGenerator(name = "restrictionSequence", initialValue = 1, sequenceName = "seq_restriction")
@JsonAdapter(RestrictionJsonAdapterFactory.class)
public abstract class AbstractRestriction implements IRestriction, IIdentifiable<Long> {

    /**
     * Internal identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "restrictionSequence")
    protected Long id;

    /**
     * Attribute restriction type
     */
    @Column(length = 32, nullable = false)
    @Enumerated(EnumType.STRING)
    protected RestrictionType type;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    @Override
    @NotNull
    public RestrictionType getType() {
        return type;
    }

    public void setType(RestrictionType pType) {
        type = pType;
    }

    @Override
    public Restriction toXml() {
        return null;
    }

    @Override
    public void fromXml(Restriction pXmlElement) {
    }
}
