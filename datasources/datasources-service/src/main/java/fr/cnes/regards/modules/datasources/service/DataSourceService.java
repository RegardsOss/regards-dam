/*
 *
 * LICENSE_PLACEHOLDER

 */
package fr.cnes.regards.modules.datasources.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.modules.plugins.service.PluginService;
import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.ModelMappingAdapter;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 *
 */
@Service
@MultitenantTransactional
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
     * GSON adapter for {@link AbstractAttributeMapping}
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
        this.service.addPluginPackage(PostgreDataSourcePlugin.class.getPackage().getName());
    }

    @Override
    public PluginConfiguration getInternalDataSource() {
        PluginConfiguration defautDataSourceConfiguration = null;

        for (PluginConfiguration pg : service.getPluginConfigurationsByType(IDataSourcePlugin.class)) {
            String val = pg.getParameterValue(IDataSourcePlugin.IS_INTERNAL_PARAM);
            if (!Strings.isNullOrEmpty(val) && IDataSourcePlugin.TRUE_INTERNAL_DATASOURCE.equalsIgnoreCase(val)) {
                defautDataSourceConfiguration = pg;
            }
        }

        return defautDataSourceConfiguration;
    }

    @Override
    public PluginConfiguration setInternalDataSource(PluginConfiguration pPluginConfiguration) throws ModuleException {

        // Get the PluginConfiguration
        PluginConfiguration newDefaultDataSourceConfiguration = service
                .getPluginConfiguration(pPluginConfiguration.getId());

        // Reset the current PluginConfiguration that is set as the internal REGARDS data source
        PluginConfiguration currentDefault = getInternalDataSource();
        currentDefault.setParameters(PluginParametersFactory.build(currentDefault.getParameters())
                .removeParameter(currentDefault.getParameter(IDataSourcePlugin.IS_INTERNAL_PARAM))
                .addParameter(IDataSourcePlugin.IS_INTERNAL_PARAM, "false").getParameters());

        // Set the PluginConfiguration as the internal REGARDS data source
        PluginParameter isInternalPlgParam = newDefaultDataSourceConfiguration
                .getParameter(IDataSourcePlugin.IS_INTERNAL_PARAM);
        newDefaultDataSourceConfiguration.setParameters(PluginParametersFactory
                .build(newDefaultDataSourceConfiguration.getParameters()).removeParameter(isInternalPlgParam)
                .addParameter(IDataSourcePlugin.IS_INTERNAL_PARAM, IDataSourcePlugin.TRUE_INTERNAL_DATASOURCE)
                .getParameters());

        return service.savePluginConfiguration(newDefaultDataSourceConfiguration);
    }

    @Override
    public List<DataSource> getAllDataSources() {
        List<DataSource> dataSources = new ArrayList<>();

        LOGGER.info("getAllDataSources");

        service.getPluginConfigurationsByType(IDataSourcePlugin.class).forEach(c -> {
            try {
                dataSources.add(getDataSourceFromPluginConfiguration(c));
            } catch (IOException e) {
                LOGGER.error("Unable to converts the PluginConfiguration <" + c.getId() + "> to a DataSource object",
                             e);
            }
        });
        return dataSources;
    }

    @Override
    public DataSource createDataSource(DataSource pDataSource) throws ModuleException {
        LOGGER.info("createDataSource : " + pDataSource.getLabel());

        try {

            if ((pDataSource.getTableName() != null) && (pDataSource.getFromClause() == null)) {
                LOGGER.info("table name : " + pDataSource.getTableName());
                return getDataSourceFromPluginConfiguration(createDataSourceFromSingleTable(pDataSource));
            }

            if ((pDataSource.getTableName() == null) && (pDataSource.getFromClause() != null)) {
                LOGGER.info("from clause : " + pDataSource.getFromClause());
                return getDataSourceFromPluginConfiguration(createDataSourceFromComplexRequest(pDataSource));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ModuleException("Unable to converts a PluginConfiguration to a Datasourceobject");
        }

        ModuleException ex = new ModuleException("The incoming datasource is inconsistent");
        LOGGER.error(ex.getMessage());
        throw ex;
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
        LOGGER.info("createDataSource : " + pDataSource.getLabel());

        PluginMetaData metaData = service.checkPluginClassName(IDataSourceFromSingleTablePlugin.class,
                                                               pDataSource.getPluginClassName());
        return service.savePluginConfiguration(new PluginConfiguration(metaData, pDataSource.getLabel(),
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
        LOGGER.info("createDataSource : " + pDataSource.getLabel());

        PluginMetaData metaData = service.checkPluginClassName(IDataSourcePlugin.class,
                                                               pDataSource.getPluginClassName());
        return service.savePluginConfiguration(new PluginConfiguration(metaData, pDataSource.getLabel(),
                buildParametersFromComplexRequest(pDataSource)));
    }

    /**
     * Crate a {@link PluginParametersFactory} with the parameters connection et model.
     *
     * @param pDataSource
     *            the {@link DataSource} to used
     * @return the {@link PluginParametersFactory} created
     * @throws ModuleException
     *             an error occurred when converts the mapping to a JSON String
     */
    private PluginParametersFactory buildParametersCommon(DataSource pDataSource) throws ModuleException {
        PluginParametersFactory factory = PluginParametersFactory.build();
        factory.addParameterPluginConfiguration(IDataSourcePlugin.CONNECTION_PARAM,
                                                dbConnectionService.getDBConnection(pDataSource
                                                        .getPluginConfigurationConnectionId()))
                .addParameter(IDataSourcePlugin.MODEL_PARAM, adapter.toJson(pDataSource.getMapping()));

        return factory;
    }

    /**
     * Create a {@link List} of {@link PluginParameter} for a pluin's type type {@link IDataSourceFromSingleTablePlugin}
     *
     * @param pDataSource
     *            the {@link DataSource} to used to create the {@link List} {@link PluginParameter}
     * @return a {@link List} of {@link PluginParameter}
     * @throws ModuleException
     *             an error occurred when converts the mapping to a JSON String
     */
    private List<PluginParameter> buildParametersSingleTable(DataSource pDataSource) throws ModuleException {
        return buildParametersCommon(pDataSource)
                .addParameter(IDataSourceFromSingleTablePlugin.TABLE_PARAM, pDataSource.getTableName()).getParameters();
    }

    /**
     * Create a {@link List} of {@link PluginParameter} for a pluin's type type {@link IDataSourcePlugin}
     *
     * @param pDataSource
     *            the {@link DataSource} to used to create the {@link List} {@link PluginParameter}
     * @return a {@link List} of {@link PluginParameter}
     * @throws ModuleException
     *             an error occurred when converts the mapping to a JSON String
     */
    private List<PluginParameter> buildParametersFromComplexRequest(DataSource pDataSource) throws ModuleException {
        return buildParametersCommon(pDataSource)
                .addParameter(IDataSourcePlugin.FROM_CLAUSE, pDataSource.getFromClause()).getParameters();
    }

    @Override
    public DataSource getDataSource(Long pId) throws EntityNotFoundException {
        LOGGER.info("getDataSource : " + pId);

        try {
            return getDataSourceFromPluginConfiguration(service.getPluginConfiguration(pId));
        } catch (ModuleException e) {
            LOGGER.error("No plugin configuration found for id:" + pId, e);
            throw new EntityNotFoundException(e.getMessage(), PluginConfiguration.class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new EntityNotFoundException("Unable to converts a PluginConfiguration to a Datasource object",
                    PluginConfiguration.class);
        }
    }

    @Override
    public DataSource updateDataSource(DataSource pDataSource) throws ModuleException {
        LOGGER.info("updateDataSource : id = {}, [new] label = {}", pDataSource.getPluginConfigurationId(), pDataSource.getLabel());

        // Get the PluginConfiguration
        PluginConfiguration plgConf = service.getPluginConfiguration(pDataSource.getPluginConfigurationId());

        // Manage the label change
        plgConf.setLabel(pDataSource.getLabel());

        // Manage the change between a DataSource from a single table and a from clause
        PluginParameter paramTableName = plgConf.getParameter(IDataSourceFromSingleTablePlugin.TABLE_PARAM);
        if ((paramTableName != null) && (pDataSource.getFromClause() != null)
                && !"".equals(pDataSource.getFromClause())) {
            plgConf.setParameters(PluginParametersFactory.build(plgConf.getParameters()).removeParameter(paramTableName)
                    .addParameterPluginConfiguration(IDataSourcePlugin.FROM_CLAUSE, null).getParameters());
        } else {
            PluginParameter paramFromClause = plgConf.getParameter(IDataSourcePlugin.FROM_CLAUSE);
            if ((paramFromClause != null) && (pDataSource.getTableName() != null)
                    && !"".equals(pDataSource.getTableName())) {
                plgConf.setParameters(PluginParametersFactory.build(plgConf.getParameters())
                        .removeParameter(paramFromClause)
                        .addParameterPluginConfiguration(IDataSourceFromSingleTablePlugin.TABLE_PARAM, null)
                        .getParameters());
            }
        }

        // Update the PluginParamater of the PluginConfiguration
        UnaryOperator<PluginParameter> unaryOpt = pn -> mergeParameters(pn, pDataSource);
        plgConf.getParameters().replaceAll(unaryOpt);

        try {
            return getDataSourceFromPluginConfiguration(service.updatePluginConfiguration(plgConf));
        } catch (IOException e) {
            LOGGER.error("Unable to converts a PluginConfiguration to a Datasource object");
            throw new ModuleException(e);
        }
    }

    @Override
    public void deleteDataSouce(Long pId) throws ModuleException {
        LOGGER.info("deleteDataSouce : " + pId);

        service.deletePluginConfiguration(pId);
    }

    /**
     * Update the {@link PluginParameter} with the appropriate {@link DataSource} attribute
     *
     * @param pPlgParam
     *            a {@link PluginParameter} to update
     * @param pDataSource
     *            a {@link DataSource}
     * @return a {{@link PluginParameter}
     * @throws ModuleException
     */
    private PluginParameter mergeParameters(PluginParameter pPlgParam, DataSource pDataSource) {
        // Update the parameter's value, because all parameters are not required for each DataSource
        pPlgParam.setValue("");

        switch (pPlgParam.getName()) {
            case IDataSourcePlugin.CONNECTION_PARAM:
                mergePluginConfigurationParameter(pPlgParam, pDataSource);
                break;
            case IDataSourcePlugin.MODEL_PARAM:
                pPlgParam.setValue(adapter.toJson(pDataSource.getMapping()));
                break;
            case IDataSourcePlugin.FROM_CLAUSE:
                pPlgParam.setValue(pDataSource.getFromClause());
                break;
            case IDataSourceFromSingleTablePlugin.TABLE_PARAM:
                pPlgParam.setValue(pDataSource.getTableName());
                break;
            default:
                break;
        }
        return pPlgParam;
    }

    /**
     * Update a {@link PluginParameter} of type connection
     *
     * @param pPlgParam
     *            a {@link PluginParameter} to update
     * @param pDataSource
     *            a {@link DataSource}
     */
    private void mergePluginConfigurationParameter(PluginParameter pPlgParam, DataSource pDataSource) {
        if ((pPlgParam.getPluginConfiguration() == null) || !pPlgParam.getPluginConfiguration().getId()
                .equals(pDataSource.getPluginConfigurationConnectionId())) {
            pPlgParam.setPluginConfiguration(null);

            try {
                pPlgParam.setPluginConfiguration(service
                        .getPluginConfiguration(pDataSource.getPluginConfigurationConnectionId()));
            } catch (ModuleException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Converts a {@link PluginConfiguration} to a {@link DataSource}.</br>
     *
     * @param pPluginConf
     *            the {@link PluginConfiguration} to converts
     * @return the {@link DataSource} created
     * @throws IOException
     *             an error occurred when converts the JSon mapping to a {@link DataSourceModelMapping}.
     */
    private DataSource getDataSourceFromPluginConfiguration(PluginConfiguration pPluginConf) throws IOException {
        DataSource dataSource = new DataSource();

        dataSource.setPluginConfigurationId(pPluginConf.getId());
        dataSource.setLabel(pPluginConf.getLabel());
        dataSource.setPluginClassName(pPluginConf.getPluginClassName());
        dataSource.setFromClause(pPluginConf.getParameterValue(IDataSourcePlugin.FROM_CLAUSE));
        dataSource.setTableName(pPluginConf.getParameterValue(IDataSourceFromSingleTablePlugin.TABLE_PARAM));
        if(pPluginConf.getParameterValue(IDataSourcePlugin.REFRESH_RATE)!=null) {
            dataSource.setRefreshRate(Integer.parseInt(pPluginConf.getParameterValue(IDataSourcePlugin.REFRESH_RATE)));
        }

        String mapping = pPluginConf.getParameterValue(IDataSourcePlugin.MODEL_PARAM);
        if (mapping != null) {
            dataSource.setMapping(adapter.fromJson(mapping));
        }

        PluginConfiguration plgConfig = pPluginConf.getParameterConfiguration(IDataSourcePlugin.CONNECTION_PARAM);
        if (plgConfig != null) {
            dataSource.setPluginConfigurationConnectionId(plgConfig.getId());
        }

        return dataSource;
    }
}
