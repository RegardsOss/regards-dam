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
package fr.cnes.regards.modules.entities.domain;

import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.framework.urn.OAISIdentifier;
import fr.cnes.regards.framework.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.models.domain.Model;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Sylvain Vissiere-Guerinet
 * @author Marc Sordi
 * @author Léo Mieulet
 *
 */
@Entity
@DiscriminatorValue("DOCUMENT")
public class Document extends AbstractEntity {

    /**
     * Physical data file references
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id", foreignKey = @ForeignKey(name = "fk_entity_data_files"))
    private List<DataFile> documents;

    public Document(Model pModel, String pTenant, String pLabel) {
        super(pModel, new UniformResourceName(OAISIdentifier.AIP, EntityType.DOCUMENT, pTenant, UUID.randomUUID(), 1),
              pLabel);
    }

    public Document() {
        super(null, null, null);
    }

    @Override
    public String getType() {
        return EntityType.DOCUMENT.toString();
    }

    public void setDocuments(List<DataFile> documents) {
        this.documents = documents;
    }

    public List<DataFile> getDocuments() {
        return documents;
    }
}
