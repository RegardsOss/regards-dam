/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

import java.util.Map;

/**
 *
 * @author Christophe Mertz
 *
 */
@FunctionalInterface
public interface IDBDataSourceColumnRepository {

    public Map<String, String> getColumns(String pTableName);
}
