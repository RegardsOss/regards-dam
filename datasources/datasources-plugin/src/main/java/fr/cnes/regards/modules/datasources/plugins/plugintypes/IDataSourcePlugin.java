/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.plugintypes;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
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
public interface IDataSourcePlugin {

    /**
     * The model parameter name
     */
    public static final String MODEL = "model";

    /**
     * The connection parameter name
     */
    public static final String CONNECTION = "connection";

    /**
     * The refresh rate of the data source
     * 
     * @return the refresh rate value
     */
    int getRefreshRate();

    /**
     * Returns true if the content of the data source has been modified.
     * 
     * @return boolean
     */
    boolean isOutOfDate();

    /**
     * Returns a {@link Page} of new entities meeting the paging restriction provided in the {@code Pageable} object.
     * 
     * @param pPageable
     *            the pagination information
     * @return a page of entities
     */
    Page<AbstractEntity> getNewData(Pageable pPageable, LocalDateTime pDate);

    /**
     * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
     * 
     * @param pPageable
     *            the pagination information
     * @return a page of entities
     */
    Page<AbstractEntity> findAll(Pageable pPageable);

}
