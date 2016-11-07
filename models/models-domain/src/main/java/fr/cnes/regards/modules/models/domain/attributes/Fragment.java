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

import fr.cnes.regards.framework.jpa.IIdentifiable;

/**
 * Fragment : gathers a set of attributes ans acts as a name space.
 *
 * @author msordi
 *
 */
@Entity
@Table(name = "T_FRAGMENT")
@SequenceGenerator(name = "fragmentSequence", initialValue = 1, sequenceName = "SEQ_FRAGMENT")
public class Fragment implements IIdentifiable<Long> {

    /**
     * Default fragment name
     */
    private static final String DEFAULT_FRAGMENT_NAME = "default";

    /**
     * Default fragment description
     */
    private static final String DEFAULT_FRAGMENT_DESCRIPTION = "Default fragment";

    /**
     * Internal identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fragmentSequence")
    private Long id;

    /**
     * Attribute name
     */
    @NotNull
    @Column(unique = true)
    private String name;

    /**
     * Optional attribute description
     */
    private String description;

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

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public boolean isDefaultFragment() {
        return DEFAULT_FRAGMENT_NAME.equals(name);
    }

    public static Fragment buildDefault() {
        final Fragment fragment = new Fragment();
        fragment.setName(getDefaultName());
        fragment.setDescription(DEFAULT_FRAGMENT_DESCRIPTION);
        return fragment;
    }

    public static String getDefaultName() {
        return DEFAULT_FRAGMENT_NAME;
    }
}
