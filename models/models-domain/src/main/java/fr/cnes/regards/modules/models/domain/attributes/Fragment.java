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

import org.hibernate.annotations.Type;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.xml.IXmlisable;

/**
 * Fragment : gathers a set of attributes and acts as a name space.
 *
 * @author msordi
 *
 */
@Entity
@Table(name = "t_fragment", indexes = { @Index(name = "idx_name", columnList = "name") })
@SequenceGenerator(name = "fragmentSequence", initialValue = 1, sequenceName = "seq_fragment")
public class Fragment implements IIdentifiable<Long>, IXmlisable<fr.cnes.regards.modules.models.schema.Fragment> {

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
     * Fragment name
     */
    @NotNull
    @Pattern(regexp = Model.NAME_REGEXP,
            message = "Fragment name must conform to regular expression \"" + Model.NAME_REGEXP + "\".")
    @Size(min = Model.NAME_MIN_SIZE, max = Model.NAME_MAX_SIZE, message = "Fragment name must be between "
            + Model.NAME_MIN_SIZE + " and " + Model.NAME_MAX_SIZE + " length.")
    @Column(unique = true, nullable = false, updatable = false, length = Model.NAME_MAX_SIZE)
    private String name;

    /**
     * Optional fragment description
     */
    @Column
    @Type(type = "text")
    private String description;

    /**
     * Optional fragment version
     */
    @Column(length = 16)
    private String version;

    // /**
    // * Fragment reference
    // */
    // @Pattern(regexp = Model.NAME_REGEXP, message = "Attribute name reference must conform to regular expression \""
    // + Model.NAME_REGEXP + "\".")
    // @Size(min = Model.NAME_MIN_SIZE, max = Model.NAME_MAX_SIZE, message = "Attribute name reference must be between "
    // + Model.NAME_MIN_SIZE + " and " + Model.NAME_MAX_SIZE + " length.")
    // @Column(name = "refname", length = Model.NAME_MAX_SIZE)
    // private String ref;

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
        return Fragment.buildFragment(getDefaultName(), DEFAULT_FRAGMENT_DESCRIPTION);
    }

    public static Fragment buildFragment(String pName, String pDescription) {
        final Fragment fragment = new Fragment();
        fragment.setName(pName);
        fragment.setDescription(pDescription);
        return fragment;
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

    @Override
    public fr.cnes.regards.modules.models.schema.Fragment toXml() {

        // CHECKSTYLE:OFF
        final fr.cnes.regards.modules.models.schema.Fragment xmlFragment = new fr.cnes.regards.modules.models.schema.Fragment();
        // CHECKSTYLE:ON
        xmlFragment.setName(name);
        xmlFragment.setDescription(description);
        xmlFragment.setVersion(version);

        return xmlFragment;
    }

    @Override
    public void fromXml(fr.cnes.regards.modules.models.schema.Fragment pXmlElement) {
        setName(pXmlElement.getName());
        setDescription(pXmlElement.getDescription());
        setVersion(pXmlElement.getVersion());
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String pVersion) {
        version = pVersion;
    }

    // public String getRef() {
    // return ref;
    // }
    //
    // public void setRef(String pRef) {
    // ref = pRef;
    // }
}
