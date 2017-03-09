/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.interfaces;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.entities.domain.DataObject;

/**
 * Class IDataSourcePlugin
 *
 * Allows to search in a data base,
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@PluginInterface(description = "Plugin to search in a data source")
public interface IDataSourcePlugin {

    /**
     * The from clause to apply to the SQL request parameter name
     */
    public static final String FROM_CLAUSE = "fromClause";

    /**
     * The model parameter name
     */
    public static final String MODEL_PARAM = "model";

    /**
     * The connection parameter name
     */
    public static final String CONNECTION_PARAM = "connection";

    /**
     * The connection parameter name
     */
    public static final String IS_INTERNAL_PARAM = "internalDataSource";

    public static final String TRUE_INTERNAL_DATASOURCE = "true";

    /**
     * The refresh rate of the data source
     *
     * @return the refresh rate value
     */
    int getRefreshRate();

    /**
     * Returns <code>true</code> if the content of the data source has been modified.
     *
     * @return boolean
     */
    boolean isOutOfDate();

    /**
     * Returns <code>true</code> if the data source is connected to internal database.
     * 
     * @return boolean
     */
    boolean isInternalDataSource();

    /**
     * Returns a {@link Page} of new entities meeting the paging restriction provided in the {@code Pageable} object.
     *
     * @param pTenant
     *            tenant to build URN
     * @param pPageable
     *            the pagination information
     * @param pDate
     *            Allows to filter the new entities created after this date parameter
     * @return a page of entities
     */
    Page<DataObject> findAll(String pTenant, Pageable pPageable, LocalDateTime pDate);

    /**
     * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
     *
     * @param pTenant
     *            tenant to build URN
     * @param pPageable
     *            the pagination information
     * @return a page of entities
     */
    Page<DataObject> findAll(String pTenant, Pageable pPageable);

}
