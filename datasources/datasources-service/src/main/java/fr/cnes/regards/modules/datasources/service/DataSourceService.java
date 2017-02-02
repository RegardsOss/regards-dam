/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.service;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.datasources.dao.IDataSourceRepository;
import fr.cnes.regards.modules.datasources.domain.DataSource;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class DataSourceService {

    private final IDataSourceRepository repository;

    public DataSourceService(IDataSourceRepository pRepository) {
        repository = pRepository;
    }

    public DataSource getDataSource(Long pDataSourceId) throws EntityNotFoundException {
        DataSource dataSource = repository.findOne(pDataSourceId);
        if (dataSource == null) {
            throw new EntityNotFoundException(pDataSourceId, DataSource.class);
        }
        return dataSource;
    }

}
