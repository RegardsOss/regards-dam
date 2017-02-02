/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.datasources.domain.DataSource;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Repository
public interface IDataSourceRepository extends CrudRepository<DataSource, Long> {

}
