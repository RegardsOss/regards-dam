/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.domain.dataaccess.accessright;

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
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;

/**
 * Access right of a group
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Entity
@Table(name = "t_access_right", uniqueConstraints = @UniqueConstraint(columnNames = { "access_group_id", "dataset_id" },
        name = "uk_access_right_access_group_id_dataset_id"))
@NamedEntityGraphs({
        @NamedEntityGraph(name = "graph.accessright.dataset.and.accessgroup",
                attributeNodes = { @NamedAttributeNode(value = "dataset"),
                        @NamedAttributeNode(value = "accessGroup", subgraph = "subgraph.accessgroup"),
                        @NamedAttributeNode(value = "dataAccessPlugin") },
                subgraphs = @NamedSubgraph(name = "subgraph.accessgroup",
                        attributeNodes = { @NamedAttributeNode(value = "users") })),
        @NamedEntityGraph(name = "graph.accessright.plugins",
                attributeNodes = { @NamedAttributeNode(value = "dataAccessPlugin") }) })
public class AccessRight implements IIdentifiable<Long> {

    /**
     * The id
     */
    @Id
    @SequenceGenerator(name = "AccessRightSequence", initialValue = 1, sequenceName = "seq_access_right")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AccessRightSequence")
    private Long id;

    /**
     * The quality filter
     */
    @Embedded
    @NotNull
    protected QualityFilter qualityFilter;

    /**
     * The access level
     */
    @Column(length = 30, name = "access_level")
    @Enumerated(EnumType.STRING)
    @NotNull
    protected AccessLevel accessLevel;

    /**
     * Plugin configuration allowing to customize the data access level
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_access_plugin", foreignKey = @ForeignKey(name = "fk_access_right_data_access_plugin"))
    private PluginConfiguration dataAccessPlugin;

    /**
     * The data access right
     */
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

    /**
     * The access group to which this access right is applied to
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_group_id", foreignKey = @ForeignKey(name = "fk_access_right_access_group_id"),
            updatable = false)
    private AccessGroup accessGroup;

    /**
     * Default constructor
     */
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

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(final Dataset pDataset) {
        dataset = pDataset;
    }

    /**
     * @return the quality filter
     */
    public QualityFilter getQualityFilter() {
        return qualityFilter;
    }

    public void setQualityFilter(final QualityFilter pQualityFilter) {
        qualityFilter = pQualityFilter;
    }

    /**
     * @return the access level
     */
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(final AccessLevel pAccessLevel) {
        accessLevel = pAccessLevel;
    }

    /**
     * @return the data access right
     */
    public DataAccessRight getDataAccessRight() {
        return dataAccessRight;
    }

    public void setDataAccessRight(final DataAccessRight pDataAccessRight) {
        dataAccessRight = pDataAccessRight;
    }

    /**
     * @return the dataset constrained
     */
    public Dataset getConstrained() {
        return dataset;
    }

    public void setConstrained(final Dataset pConstrained) {
        dataset = pConstrained;
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
     * @return the access group
     */
    public AccessGroup getAccessGroup() {
        return accessGroup;
    }

    public void setAccessGroup(final AccessGroup pAccessGroup) {
        accessGroup = pAccessGroup;
    }

    public PluginConfiguration getDataAccessPlugin() {
        return dataAccessPlugin;
    }

    public void setDataAccessPlugin(PluginConfiguration dataAccessPlugin) {
        this.dataAccessPlugin = dataAccessPlugin;
    }

}
