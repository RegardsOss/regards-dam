/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessgroup;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.dataaccess.domain.accessright.GroupAccessRight;
import fr.cnes.regards.modules.dataaccess.domain.jpa.converters.UserConverter;

/**
 * Entity representing an group of user having rights on some data
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
@Table(name = "T_ACCESS_GROUP")
public class AccessGroup implements IIdentifiable<Long> {

    @Id
    @SequenceGenerator(name = "AccessGroupSequence", initialValue = 1, sequenceName = "SEQ_ACCESS_GROUP")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AccessGroupSequence")
    private Long id;

    @NotNull
    @Column(unique = true, updatable = false)
    private String name;

    @NotNull
    @ElementCollection
    @CollectionTable(name = "TA_ACCESS_GROUP_USERS", joinColumns = @JoinColumn(name = "users_email"))
    @Convert(converter = UserConverter.class)
    private Set<User> users;

    @NotNull
    @OneToMany
    private Set<GroupAccessRight> accesRights;

    private boolean isPrivate = Boolean.TRUE;

    public AccessGroup() {
        // for hibernate
    }

    public AccessGroup(String pName) {
        super();
        users = new HashSet<>();
        accesRights = new HashSet<>();
        name = pName;
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

    public Set<GroupAccessRight> getAccesRights() {
        return accesRights;
    }

    public void setName(String pName) {
        name = pName;
    }

    public void addUser(User pUser) {
        users.add(pUser);
    }

    public void removeUser(User pUser) {
        users.remove(pUser);
    }

    public void setAccesRights(Set<GroupAccessRight> pAccesRights) {
        accesRights = pAccesRights;
    }

    public Set<User> getUsers() {
        return users;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean pIsPrivate) {
        isPrivate = pIsPrivate;
    }

    public void setUsers(Set<User> pUsers) {
        users = pUsers;
    }

    @Override
    public boolean equals(Object pOther) {
        return (pOther instanceof AccessGroup) && ((AccessGroup) pOther).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
