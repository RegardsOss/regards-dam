/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.plugintypes;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.datasources.plugins.repository.IEntityPagingAndSortingRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * Class IDataSourcePlugin
 *
 * Allows to manage data sources
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@PluginInterface(description = "Plugin to search in a data source")
public interface IDataSourcePlugin extends IEntityPagingAndSortingRepository<AbstractEntity> {

    public static final String MODEL = "model";

    int getRefreshRate();

}
