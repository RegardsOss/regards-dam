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

import java.util.Collection;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.modules.entities.domain.feature.DocumentFeature;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Document feature decorator
 *
 * @author Sylvain Vissiere-Guerinet
 * @author Marc Sordi
 * @author Léo Mieulet
 */
@Entity
@DiscriminatorValue("DOCUMENT")
public class Document extends AbstractEntity<DocumentFeature> {

    public Document() {
        super(null, null);
    }

    public Document(Model model, String tenant, String label) {
        super(model, new DocumentFeature(tenant, label));
    }

    public Collection<DataFile> getDocumentFiles() {
        return getFiles().get(DataType.DOCUMENT);
    }

}
