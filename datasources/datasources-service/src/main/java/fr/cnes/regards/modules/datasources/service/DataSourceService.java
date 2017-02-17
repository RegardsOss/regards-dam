/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.datasources.domain.DataSource;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 *
 */
@Service
public class DataSourceService implements IDataSourceService {

    public DataSourceService() {
    }

    /**
     * @return
     */
    public PluginConfiguration getDefaultDataSource() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<PluginConfiguration> getAllDataSources() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PluginConfiguration createDataSource(DataSource pDataSource) throws ModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PluginConfiguration getDataSource(Long pId) throws EntityNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PluginConfiguration updateDataSource(DataSource pDataSource) throws ModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteDataSouce(Long pId) throws ModuleException {
        // TODO Auto-generated method stub

    }

    @Override
    public void getTables(Long pId) throws ModuleException {
        // TODO Auto-generated method stub

    }

    @Override
    public void getColumns(Long pId) throws ModuleException {
        // TODO Auto-generated method stub

    }

    @Override
    public void getIndeses(Long pId) throws ModuleException {
        // TODO Auto-generated method stub

    }

}
