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
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.entities.domain.Dataset;

/**
 * Access right of a group
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Entity
@Table(name = "t_access_right",
        uniqueConstraints = @UniqueConstraint(columnNames = { "access_group_id", "dataset_id" }, name = "uk_access_right_access_group_id_dataset_id"))
@NamedEntityGraph(name = "graph.accessright.dataset.and.accesgroup",
        attributeNodes = { @NamedAttributeNode(value = "dataset"), @NamedAttributeNode(value = "accessGroup") })
public class AccessRight implements IIdentifiable<Long> {

    @Id
    @SequenceGenerator(name = "AccessRightSequence", initialValue = 1, sequenceName = "seq_access_right")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AccessRightSequence")
    private Long id;

    @Embedded
    @NotNull
    protected QualityFilter qualityFilter;

    @Column(length = 30, name = "access_level")
    @Enumerated(EnumType.STRING)
    @NotNull
    protected AccessLevel accessLevel;

    @Embedded
    protected DataAccessRight dataAccessRight;

    /**
     * It is mandatory to have no cascade at all on Dataset (a Dataset CRUD must be done through DatasetService)
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", foreignKey = @ForeignKey(name = "fk_access_right_access_dataset_id"),
            updatable = false)
    private Dataset dataset;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_group_id", foreignKey = @ForeignKey(name = "fk_access_right_access_group_id"),
            updatable = false)
    private AccessGroup accessGroup;

    protected AccessRight() {
    }

    public AccessRight(final QualityFilter pQualityFilter, final AccessLevel pAccessLevel, final Dataset pDataset,
            final AccessGroup pAccessGroup) {
        super();
        qualityFilter = pQualityFilter;
        accessLevel = pAccessLevel;
        dataset = pDataset;
        accessGroup = pAccessGroup;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(final Dataset pDataset) {
        dataset = pDataset;
    }

    public QualityFilter getQualityFilter() {
        return qualityFilter;
    }

    public void setQualityFilter(final QualityFilter pQualityFilter) {
        qualityFilter = pQualityFilter;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(final AccessLevel pAccessLevel) {
        accessLevel = pAccessLevel;
    }

    public DataAccessRight getDataAccessRight() {
        return dataAccessRight;
    }

    public void setDataAccessRight(final DataAccessRight pDataAccessRight) {
        dataAccessRight = pDataAccessRight;
    }

    public Dataset getConstrained() {
        return dataset;
    }

    public void setConstrained(final Dataset pConstrained) {
        dataset = pConstrained;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long pId) {
        id = pId;
    }

    public AccessGroup getAccessGroup() {
        return accessGroup;
    }

    public void setAccessGroup(final AccessGroup pAccessGroup) {
        accessGroup = pAccessGroup;
    }

}
