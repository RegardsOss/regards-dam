/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.adapters.gson;

import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapterFactory;
import fr.cnes.regards.modules.entities.domain.DataSet;

/**
 *
 * {@link DataSet} adapter factory
 *
 * @author Marc Sordi
 *
 */
@GsonTypeAdapterFactory
public class DatasetTypeAdapterFactory extends AbstractEntityTypeAdapterFactory<DataSet> {

    public DatasetTypeAdapterFactory() {
        super(DataSet.class);
    }
}
