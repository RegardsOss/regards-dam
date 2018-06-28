/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.entities.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Document locally stored on rs-dam
 * @author Léo Mieulet
 */
@Entity
@Table(name = "t_local_storage", uniqueConstraints = @UniqueConstraint(columnNames = { "entity_id", "file_checksum" },
        name = "uk_t_local_storage_document_file_checksum"))
@SequenceGenerator(name = "localStorageSequence", initialValue = 1, sequenceName = "documentLS_Sequence")
public class LocalFile {

    /**
     * Internal identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "localStorageSequence")
    private Long id;

    /**
     * Related document
     */
    @ManyToOne
    @JoinColumn(name = "entity_id", foreignKey = @ForeignKey(name = "fk_ls_entity_id"), nullable = false,
            updatable = false)
    private AbstractEntity<?> entity;

    /**
     * File checksum
     */
    @Column(name = "file_checksum", nullable = false, updatable = false)
    private String fileChecksum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }

    public void setFileChecksum(String fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

    public AbstractEntity<?> getEntity() {
        return entity;
    }

    public void setEntity(AbstractEntity<?> entity) {
        this.entity = entity;
    }

    public static LocalFile build(AbstractEntity<?> entity, String fileChecksum) {
        LocalFile ls = new LocalFile();
        ls.setEntity(entity);
        ls.setFileChecksum(fileChecksum);
        return ls;
    }

}
