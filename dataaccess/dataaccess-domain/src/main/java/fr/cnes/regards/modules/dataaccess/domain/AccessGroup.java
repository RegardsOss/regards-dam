/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
public class AccessGroup {

    @Id
    @SequenceGenerator(name = "AccessGroupSequence", initialValue = 1, sequenceName = "SEQ_ACCESS_GROUP")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AccessGroupSequence")
    private Long id;

    @NotNull
    @Column(unique = true, updatable = false)
    private final String name;

    @NotNull
    @OneToMany
    private final Set<User> subscribers;

    @NotNull
    @OneToMany
    private final Set<AccessRight> accesRights;

    public AccessGroup(String pName) {
        super();
        subscribers = new HashSet<>();
        accesRights = new HashSet<>();
        name = pName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public String getName() {
        return name;
    }

    public Set<User> getSubscribers() {
        return subscribers;
    }

    public Set<AccessRight> getAccesRights() {
        return accesRights;
    }

}
