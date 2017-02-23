/*
 * 
 * LICENSE_PLACEHOLDER

 */
package fr.cnes.regards.modules.datasources.service;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.modules.plugins.service.PluginService;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.Index;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.datasources.utils.ModelMappingAdapter;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 *
 */
@Service
public class DataSourceService implements IDataSourceService {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceService.class);

    /**
     * Attribute {@link PluginService}
     */
    private final IPluginService service;

    /**
     * Attribute {@link DBConnectionService}
     */
    private final IDBConnectionService dbConnectionService;

    /**
     * GSON adapter for {@link DataSourceAttributeMapping}
     */
    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    /**
     * The constructor with an instance of the {@link PluginService}
     * 
     * @param pPluginService
     *            The {@link PluginService} to used by this service
     * @param pDBConnectionService
     *            The {@link DBConnectionService} to used by this service
     */
    public DataSourceService(IPluginService pPluginService, IDBConnectionService pDBConnectionService) {
        super();
        this.service = pPluginService;
        this.dbConnectionService = pDBConnectionService;
        this.service.addPluginPackage("fr.cnes.regards.modules.datasources.plugins");
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
        return service.getPluginConfigurationsByType(IDataSourcePlugin.class);
    }

    @Override
    public PluginConfiguration createDataSource(DataSource pDataSource) throws ModuleException {

        if (pDataSource.getTableName() != null && pDataSource.getFromClause() == null) {
            return createDataSourceFromSingleTable(pDataSource);
        }

        if (pDataSource.getTableName() == null && pDataSource.getFromClause() != null) {
            return createDataSourceFromComplexRequest(pDataSource);
        }

        throw new ModuleException("The datasource is inconsistent.");
    }

    /**
     * Create a {@link PluginConfiguration} for a plugin's type {@link IDataSourceFromSingleTablePlugin} with a
     * {@link DataSource}.
     * 
     * @param pDataSource
     *            the {@link DataSource} to used to create the {@link PluginConfiguration}
     * @return the {@link PluginConfiguration} created
     * @throws ModuleException
     */
    private PluginConfiguration createDataSourceFromSingleTable(DataSource pDataSource) throws ModuleException {
        PluginMetaData metaData = service.checkPluginClassName(IDataSourceFromSingleTablePlugin.class,
                                                               pDataSource.getPluginClassName());
        return service.savePluginConfiguration(new PluginConfiguration(metaData, pDataSource.getPluginClassName(),
                buildParametersSingleTable(pDataSource)));
    }

    /**
     * Create a {@link PluginConfiguration} for a plugin's type {@link IDataSourcePlugin} with a {@link DataSource}
     * 
     * @param pDataSource
     *            the {@link DataSource} to used to create the {@link PluginConfiguration}
     * @return the {@link PluginConfiguration} created
     * @throws ModuleException
     */
    private PluginConfiguration createDataSourceFromComplexRequest(DataSource pDataSource) throws ModuleException {
        PluginMetaData metaData = service.checkPluginClassName(IDataSourcePlugin.class,
                                                               pDataSource.getPluginClassName());
        return service.savePluginConfiguration(new PluginConfiguration(metaData, pDataSource.getPluginClassName(),
                buildParametersFromComplexRequest(pDataSource)));
    }

    /**
     * Create a {@link List} of {@link PluginParameter} for a pluin's type type {@link IDataSourceFromSingleTablePlugin}
     * 
     * @param pDataSource
     *            the {@link DataSource} to used to create the {@link List} {@link PluginParameter}
     * @return a {@link List} of {@link PluginParameter}
     * @throws ModuleException
     */
    private List<PluginParameter> buildParametersSingleTable(DataSource pDataSource) throws ModuleException {
        PluginParametersFactory factory = PluginParametersFactory.build();
        factory.addParameterPluginConfiguration(IDataSourceFromSingleTablePlugin.CONNECTION_PARAM, dbConnectionService
                .getDBConnection(pDataSource.getPluginConfigurationConnectionId()));
        if (pDataSource.getMapping() != null) {
            factory.addParameter(IDataSourceFromSingleTablePlugin.MODEL_PARAM,
                                 adapter.toJson(pDataSource.getMapping()));
        }
        return factory.getParameters();
    }

    /**
     * Create a {@link List} of {@link PluginParameter} for a pluin's type type {@link IDataSourcePlugin}
     * 
     * @param pDataSource
     *            the {@link DataSource} to used to create the {@link List} {@link PluginParameter}
     * @return a {@link List} of {@link PluginParameter}
     * @throws ModuleException
     */
    private List<PluginParameter> buildParametersFromComplexRequest(DataSource pDataSource) throws ModuleException {
        PluginParametersFactory factory = PluginParametersFactory.build();
        factory.addParameterPluginConfiguration(IDataSourcePlugin.CONNECTION_PARAM,
                                                dbConnectionService.getDBConnection(pDataSource
                                                        .getPluginConfigurationConnectionId()))
                .addParameter(IDataSourcePlugin.FROM_CLAUSE, pDataSource.getFromClause())
                .addParameter(IDataSourcePlugin.MODEL_PARAM, adapter.toJson(pDataSource.getMapping()));

        return factory.getParameters();
    }

    @Override
    public PluginConfiguration getDataSource(Long pId) throws EntityNotFoundException {

        try {
            return service.getPluginConfiguration(pId);
        } catch (ModuleException e) {
            LOGGER.error("No plugin configuration found for id:" + pId, e);
            throw new EntityNotFoundException(e.getMessage(), PluginConfiguration.class);
        }
    }

    @Override
    public PluginConfiguration updateDataSource(DataSource pDataSource) throws ModuleException {
        // Get the PluginConfiguration
        PluginConfiguration plgConf = service.getPluginConfiguration(pDataSource.getPluginConfigurationId());

        // Update the PluginParamater of the PluginConfiguration
        UnaryOperator<PluginParameter> unaryOpt = pn -> mergeParameters(pn, pDataSource);
        plgConf.getParameters().replaceAll(unaryOpt);

        // TODO CMZ gérer le changement de type de plugin

        return service.updatePluginConfiguration(plgConf);
    }

    @Override
    public void deleteDataSouce(Long pId) throws ModuleException {
        service.deletePluginConfiguration(pId);
    }

    @Override
    public Map<String, Table> getTables(Long pId) throws ModuleException {
        IDataSourceFromSingleTablePlugin plg = service.getPlugin(pId);
        return plg.getTables();
    }

    @Override
    public Map<String, Column> getColumns(Long pId, Table pTable) throws ModuleException {
        IDataSourceFromSingleTablePlugin plg = service.getPlugin(pId);
        return plg.getColumns(pTable);
    }

    @Override
    public Map<String, Index> getIndexes(Long pId, Table pTable) throws ModuleException {
        IDataSourceFromSingleTablePlugin plg = service.getPlugin(pId);
        return plg.getIndexes(pTable);
    }

    /**
     * Update the {@link PluginParameter} with the appropriate {@link DataSource} attribute
     * 
     * @param pPlgParam
     *            a {@link PluginParameter}
     * @param pDataSource
     *            A {@link DataSource}
     * @return a {{@link PluginParameter}
     */
    private PluginParameter mergeParameters(PluginParameter pPlgParam, DataSource pDataSource) {
        // Update the parameter's value, because all parameters are not required for each DataSource
        pPlgParam.setValue("");
        switch (pPlgParam.getName()) {
            case IDataSourceFromSingleTablePlugin.CONNECTION_PARAM:
                pPlgParam.setValue(pDataSource.getPluginConfigurationConnectionId().toString());
                break;
            case IDataSourceFromSingleTablePlugin.MODEL_PARAM:
                pPlgParam.setValue(adapter.toJson(pDataSource.getMapping()));
                break;
            case IDataSourceFromSingleTablePlugin.FROM_CLAUSE:
                pPlgParam.setValue(pDataSource.getFromClause());
                break;
            default:
                break;
        }
        return pPlgParam;
    }
}
