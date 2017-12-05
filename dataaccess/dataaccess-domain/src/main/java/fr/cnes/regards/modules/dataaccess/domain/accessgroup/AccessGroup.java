/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.dataaccess.domain.accessgroup;

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
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

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
@Table(name = "t_access_group",
        uniqueConstraints = @UniqueConstraint(name = "uk_access_group_name", columnNames = { "name" }))
@NamedEntityGraph(name = "graph.accessgroup.users", attributeNodes = @NamedAttributeNode(value="users"))
public class AccessGroup implements IIdentifiable<Long> {

    /**
     * Name regular expression
     */
    public static final String NAME_REGEXP = "[a-zA-Z_][0-9a-zA-Z_]*";

    /**
     * Name min size
     */
    public static final int NAME_MIN_SIZE = 3;

    /**
     * Name max size
     */
    public static final int NAME_MAX_SIZE = 32;

    /**
     * the id
     */
    @Id
    @SequenceGenerator(name = "AccessGroupSequence", initialValue = 1, sequenceName = "seq_access_group")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AccessGroupSequence")
    private Long id;

    /**
     * The name
     */
    @NotNull
    @Pattern(regexp = NAME_REGEXP, message = "Group name must conform to regular expression \"" + NAME_REGEXP + "\".")
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE,
            message = "Group name must be between " + NAME_MIN_SIZE + " and " + NAME_MAX_SIZE + " length.")
    @Column(length = NAME_MAX_SIZE, updatable = false)
    private String name;

    @NotNull
    @ElementCollection
    @CollectionTable(name = "ta_access_group_users", joinColumns = @JoinColumn(name = "access_group_id"),
            foreignKey = @ForeignKey(name = "fk_access_group_users"))
    @Convert(converter = UserConverter.class)
    private Set<User> users = new HashSet<>();

    /**
     * Is the group public?
     */
    @Column(name = "public")
    private boolean isPublic = Boolean.FALSE;

    @Column(name = "internal")
    private boolean isInternal = Boolean.FALSE;

    public AccessGroup() {
        super();
        name = "";
    }

    public AccessGroup(final String pName) {
        super();
        name = pName;
    }

    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long pId) {
        id = pId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void setName(final String pName) {
        name = pName;
    }

    public void addUser(final User pUser) {
        users.add(pUser);
    }

    public void removeUser(final User pUser) {
        users.remove(pUser);
    }

    /**
     * @return the users
     */
    public Set<User> getUsers() {
        return users;
    }

    /**
     * @return whether the group is public or not
     */
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(final boolean pIsPublic) {
        isPublic = pIsPublic;
    }

    public void setUsers(final Set<User> pUsers) {
        users = pUsers;
    }

    public void setInternal(boolean isInternal) {
        this.isInternal = isInternal;
    }

    public boolean isInternal() {
        return isInternal;
    }

    @Override
    public boolean equals(final Object pOther) {
        return (pOther instanceof AccessGroup) && ((AccessGroup) pOther).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
