/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.adapters.gson;

import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapterFactory;
import fr.cnes.regards.modules.entities.domain.Collection;

/**
 *
 * {@link Collection} adapter factory
 *
 * @author Marc Sordi
 *
 */
@GsonTypeAdapterFactory
public class CollectionTypeAdapterFactory extends AbstractEntityTypeAdapterFactory<Collection> {

    public CollectionTypeAdapterFactory() {
        super(Collection.class);
    }
}
