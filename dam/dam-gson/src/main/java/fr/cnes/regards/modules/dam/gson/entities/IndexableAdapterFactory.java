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
package fr.cnes.regards.modules.dam.gson.entities;

import fr.cnes.regards.framework.gson.adapters.PolymorphicTypeAdapterFactory;
import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapterFactory;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.Document;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.indexer.domain.reminder.SearchAfterReminder;

/**
 * IIndexable document adapter factory
 * @author Marc Sordi
 */
@GsonTypeAdapterFactory
public class IndexableAdapterFactory extends PolymorphicTypeAdapterFactory<IIndexable> {

    /**
     * Constructor
     * <b>Note: type needs to be injected</b>
     */
    public IndexableAdapterFactory() {
        super(IIndexable.class, "type", true);
        // AbstractEntities
        registerSubtype(Collection.class, EntityType.COLLECTION);
        registerSubtype(Dataset.class, EntityType.DATASET);
        registerSubtype(Document.class, EntityType.DOCUMENT);
        registerSubtype(DataObject.class, EntityType.DATA, true);
        // Reminder
        registerSubtype(SearchAfterReminder.class, SearchAfterReminder.TYPE);
    }
}
