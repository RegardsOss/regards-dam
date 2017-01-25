/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.utils;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 
 *
 * @author Christophe Mertz
 */
public interface IDomainDataSourceRepository extends JpaRepository<DataSourceEntity, Long> {

}
