/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes;

import javax.persistence.Column;
import javax.persistence.Entity;
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
 * Fragment : gathers a set of attributes ans acts as a name space.
 *
 * @author msordi
 *
 */
@Entity
@Table(name = "T_FRAGMENT", indexes = { @Index(name = "IDX_NAME", columnList = "name") })
@SequenceGenerator(name = "fragmentSequence", initialValue = 1, sequenceName = "SEQ_FRAGMENT")
public class Fragment implements IIdentifiable<Long> {

    /**
     * Name regular expression
     */
    public static final String FRAGMENT_NAME_REGEXP = "[0-9a-zA-Z_]*";

    /**
     * Name min size
     */
    public static final int FRAGMENT_NAME_MIN_SIZE = 3;

    /**
     * Name max size
     */
    public static final int FRAGMENT_NAME_MAX_SIZE = 20;

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
    @Pattern(regexp = FRAGMENT_NAME_REGEXP, message = "Fragment name must conform to regular expression \""
            + FRAGMENT_NAME_REGEXP + "\".")
    @Size(min = FRAGMENT_NAME_MIN_SIZE, max = FRAGMENT_NAME_MAX_SIZE, message = "Fragment name must be between "
            + FRAGMENT_NAME_MIN_SIZE + " and " + FRAGMENT_NAME_MAX_SIZE + " length.")
    @Column(unique = true, nullable = false, updatable = false)
    private String name;

    /**
     * Optional attribute description
     */
    private String description;

    // Useful for (de)serialization
    public Fragment() {
        super();
    }

    public Fragment(String pName, String pDescription) {
        name = pName;
        description = pDescription;
    }

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
        return new Fragment(getDefaultName(), DEFAULT_FRAGMENT_DESCRIPTION);
    }

    public static Fragment buildFragment(String pName, String pDescription) {
        return new Fragment(pName, pDescription);
    }

    public static String getDefaultName() {
        return DEFAULT_FRAGMENT_NAME;
    }

    @Override
    public boolean equals(Object pObj) {
        if (Fragment.class.isInstance(pObj)) {
            final Fragment f = (Fragment) pObj;
            if ((f == null) || (f.getName() == null)) {
                return false;
            }
            return f.getName().equals(name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (name == null) {
            return 0;
        }
        return name.hashCode();
    }
}
