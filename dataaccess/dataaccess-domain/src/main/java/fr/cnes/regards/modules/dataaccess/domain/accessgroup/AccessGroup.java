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
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.dataaccess.domain.jpa.converters.UserConverter;

/**
 * Entity representing an group of user having rights on some data
 *
 * @author Sylvain Vissiere-Guerinet
 *
 *         FIXME: for V2 or whenever users will be granted rights: add into THIS class a way to distinguish a group
 *         which name is an email and so is a "fake" group only linked to a user. isUserGroup maybe, to be established
 *         with the front
 */
@Entity
@Table(name = "t_access_group")
public class AccessGroup implements IIdentifiable<Long> {

    @Id
    @SequenceGenerator(name = "AccessGroupSequence", initialValue = 1, sequenceName = "SEQ_ACCESS_GROUP")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AccessGroupSequence")
    private Long id;

    @NotNull
    @Column(length = 32, unique = true, updatable = false)
    private String name;

    @NotNull
    @ElementCollection
    @CollectionTable(name = "ta_access_group_users", joinColumns = @JoinColumn(name = "access_group_id"),
            foreignKey = @ForeignKey(name = "fk_access_group_users"))
    @Convert(converter = UserConverter.class)
    private Set<User> users = new HashSet<>();

    @Column(name = "public")
    private boolean isPublic = Boolean.FALSE;

    @SuppressWarnings("unused")
    private AccessGroup() { // NOSONAR
    }

    public AccessGroup(String pName) {
        super();
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

    public void setName(String pName) {
        name = pName;
    }

    public void addUser(User pUser) {
        users.add(pUser);
    }

    public void removeUser(User pUser) {
        users.remove(pUser);
    }

    public Set<User> getUsers() {
        return users;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean pIsPublic) {
        isPublic = pIsPublic;
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
