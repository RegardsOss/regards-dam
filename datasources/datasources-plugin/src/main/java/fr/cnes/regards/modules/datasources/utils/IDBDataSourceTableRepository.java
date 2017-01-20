/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

import java.util.List;

/**
 *
 * @author Christophe Mertz
 *
 */
@FunctionalInterface
public interface IDBDataSourceTableRepository {

    public List<String> getTables();
}
