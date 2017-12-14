/*
 *
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package fr.cnes.regards.modules.datasources.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.modules.plugins.service.PluginService;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;
import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.ModelMappingAdapter;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
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
     * @param pluginService The {@link PluginService} to used by this service
     * @param dbConnectionService The {@link DBConnectionService} to used by this service
     */
    public DataSourceService(IPluginService pluginService, IDBConnectionService dbConnectionService) {
        super();
        this.service = pluginService;
        this.dbConnectionService = dbConnectionService;
        this.service.addPluginPackage(PostgreDataSourcePlugin.class.getPackage().getName());
    }

    @Override
    public List<DataSource> getAllDataSources() {
        List<DataSource> dataSources = new ArrayList<>();

        LOGGER.info("getAllDataSources");

        service.getPluginConfigurationsByType(IDataSourcePlugin.class).forEach(c -> {
            try {
                dataSources.add(getDataSourceFromPluginConfiguration(c));
            } catch (IOException e) {
                LOGGER.error("Unable to convert the PluginConfiguration <" + c.getId() + "> to a DataSource object", e);
            }
        });
        return dataSources;
    }

    @Override
    public DataSource createDataSource(DataSource dataSource) throws ModuleException {
        LOGGER.info("createDataSource : " + dataSource.getLabel());

        try {

            if ((dataSource.getTableName() != null) && (dataSource.getFromClause() == null)) {
                LOGGER.info("table name : " + dataSource.getTableName());
                return getDataSourceFromPluginConfiguration(createDataSourceFromSingleTable(dataSource));
            }

            if ((dataSource.getTableName() == null) && (dataSource.getFromClause() != null)) {
                LOGGER.info("from clause : " + dataSource.getFromClause());
                return getDataSourceFromPluginConfiguration(createDataSourceFromComplexRequest(dataSource));
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
     * Create a {@link PluginConfiguration} for a plugin's type {@link IDBDataSourceFromSingleTablePlugin} with a
     * {@link DataSource}.
     * @param dataSource the {@link DataSource} to used to create the {@link PluginConfiguration}
     * @return the {@link PluginConfiguration} created
     */
    private PluginConfiguration createDataSourceFromSingleTable(DataSource dataSource) throws ModuleException {
        LOGGER.info("createDataSource : " + dataSource.getLabel());

        PluginMetaData metaData = service.checkPluginClassName(IDBDataSourceFromSingleTablePlugin.class,
                                                               dataSource.getPluginClassName());
        return service.savePluginConfiguration(new PluginConfiguration(metaData, dataSource.getLabel(),
                buildParametersSingleTable(dataSource)));
    }

    /**
     * Create a {@link PluginConfiguration} for a plugin's type {@link IDataSourcePlugin} with a {@link DataSource}
     * @param dataSource the {@link DataSource} to used to create the {@link PluginConfiguration}
     * @return the {@link PluginConfiguration} created
     */
    private PluginConfiguration createDataSourceFromComplexRequest(DataSource dataSource) throws ModuleException {
        LOGGER.info("createDataSource : " + dataSource.getLabel());

        PluginMetaData metaData = service.checkPluginClassName(IDataSourcePlugin.class,
                                                               dataSource.getPluginClassName());
        return service.savePluginConfiguration(new PluginConfiguration(metaData, dataSource.getLabel(),
                buildParametersFromComplexRequest(dataSource)));
    }

    /**
     * Crate a {@link PluginParametersFactory} with the parameters connection et model.
     * @param dataSource the {@link DataSource} to used
     * @return the {@link PluginParametersFactory} created
     * @throws ModuleException an error occurred when converts the mapping to a JSON String
     */
    private PluginParametersFactory buildParametersCommon(DataSource dataSource) throws ModuleException {
        PluginParametersFactory factory = PluginParametersFactory.build();
        factory.addPluginConfiguration(IDBDataSourcePlugin.CONNECTION_PARAM,
                                       dbConnectionService
                                               .getDBConnection(dataSource.getPluginConfigurationConnectionId()))
                .addParameter(IDBDataSourcePlugin.MODEL_PARAM, dataSource.getMapping())
                .addParameter(IDataSourcePlugin.REFRESH_RATE,
                              (dataSource.getRefreshRate() == null) ? IDataSourcePlugin.REFRESH_RATE_DEFAULT_VALUE
                                      : dataSource.getRefreshRate());

        return factory;
    }

    /**
     * Create a {@link List} of {@link PluginParameter} for a pluin's type type
     * {@link IDBDataSourceFromSingleTablePlugin}
     * @param dataSource the {@link DataSource} to used to create the {@link List} {@link PluginParameter}
     * @return a {@link List} of {@link PluginParameter}
     * @throws ModuleException an error occurred when converts the mapping to a JSON String
     */
    private List<PluginParameter> buildParametersSingleTable(DataSource dataSource) throws ModuleException {
        return buildParametersCommon(dataSource)
                .addParameter(IDBDataSourceFromSingleTablePlugin.TABLE_PARAM, dataSource.getTableName())
                .getParameters();
    }

    /**
     * Create a {@link List} of {@link PluginParameter} for a pluin's type type {@link IDataSourcePlugin}
     * @param dataSource the {@link DataSource} to used to create the {@link List} {@link PluginParameter}
     * @return a {@link List} of {@link PluginParameter}
     * @throws ModuleException an error occurred when converts the mapping to a JSON String
     */
    private List<PluginParameter> buildParametersFromComplexRequest(DataSource dataSource) throws ModuleException {
        return buildParametersCommon(dataSource)
                .addParameter(IDBDataSourcePlugin.FROM_CLAUSE, dataSource.getFromClause()).getParameters();
    }

    @Override
    public DataSource getDataSource(Long id) throws EntityNotFoundException {
        LOGGER.info("getDataSource : " + id);

        try {
            return getDataSourceFromPluginConfiguration(service.getPluginConfiguration(id));
        } catch (ModuleException e) {
            LOGGER.error("No plugin configuration found for id:" + id, e);
            throw new EntityNotFoundException(e.getMessage(), PluginConfiguration.class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new EntityNotFoundException("Unable to converts a PluginConfiguration to a Datasource object",
                    PluginConfiguration.class);
        }
    }

    @Override
    public DataSource updateDataSource(DataSource dataSource) throws ModuleException {
        LOGGER.info("updateDataSource : id = {}, [new] label = {}", dataSource.getPluginConfigurationId(),
                    dataSource.getLabel());

        // Get the PluginConfiguration
        PluginConfiguration plgConf = service.getPluginConfiguration(dataSource.getPluginConfigurationId());

        // Manage the label change
        plgConf.setLabel(dataSource.getLabel());

        // Manage the change between a DataSource from a single table and a from clause
        PluginParameter paramTableName = plgConf.getParameter(IDBDataSourceFromSingleTablePlugin.TABLE_PARAM);
        if ((paramTableName != null) && (dataSource.getFromClause() != null)
                && !"".equals(dataSource.getFromClause())) {
            plgConf.setParameters(PluginParametersFactory.build(plgConf.getParameters()).removeParameter(paramTableName)
                    .addPluginConfiguration(IDBDataSourcePlugin.FROM_CLAUSE, null).getParameters());
        } else {
            PluginParameter paramFromClause = plgConf.getParameter(IDBDataSourcePlugin.FROM_CLAUSE);
            if ((paramFromClause != null) && (dataSource.getTableName() != null)
                    && !"".equals(dataSource.getTableName())) {
                plgConf.setParameters(PluginParametersFactory.build(plgConf.getParameters())
                        .removeParameter(paramFromClause)
                        .addPluginConfiguration(IDBDataSourceFromSingleTablePlugin.TABLE_PARAM, null).getParameters());
            }
        }

        // Update the PluginParamater of the PluginConfiguration
        UnaryOperator<PluginParameter> unaryOpt = pn -> mergeParameters(pn, dataSource);
        plgConf.getParameters().replaceAll(unaryOpt);

        try {
            return getDataSourceFromPluginConfiguration(service.updatePluginConfiguration(plgConf));
        } catch (IOException e) {
            LOGGER.error("Unable to converts a PluginConfiguration to a Datasource object");
            throw new ModuleException(e);
        }
    }

    @Override
    public void deleteDataSouce(Long id) throws ModuleException {
        LOGGER.info("deleteDataSouce : " + id);

        service.deletePluginConfiguration(id);
    }

    /**
     * Update the {@link PluginParameter} with the appropriate {@link DataSource} attribute
     * @param pluginParam a {@link PluginParameter} to update
     * @param dataSource a {@link DataSource}
     * @return a {{@link PluginParameter}
     */
    private PluginParameter mergeParameters(PluginParameter pluginParam, DataSource dataSource) {
        // Update the parameter's value, because all parameters are not required for each DataSource
        pluginParam.setValue("");

        switch (pluginParam.getName()) {
            case IDBDataSourcePlugin.CONNECTION_PARAM:
                mergePluginConfigurationParameter(pluginParam, dataSource);
                break;
            case IDBDataSourcePlugin.MODEL_PARAM:
                pluginParam.setValue(adapter.toJson(dataSource.getMapping()));
                break;
            case IDBDataSourcePlugin.FROM_CLAUSE:
                pluginParam.setValue(dataSource.getFromClause());
                break;
            case IDBDataSourceFromSingleTablePlugin.TABLE_PARAM:
                pluginParam.setValue(dataSource.getTableName());
                break;
            case IDataSourcePlugin.REFRESH_RATE:
                pluginParam.setValue(dataSource.getRefreshRate() == null
                        ? IDataSourcePlugin.REFRESH_RATE_DEFAULT_VALUE_AS_STRING
                        : dataSource.getRefreshRate().toString());
                break;
            default:
                break;
        }
        return pluginParam;
    }

    /**
     * Update a {@link PluginParameter} of type connection
     * @param pluginParam a {@link PluginParameter} to update
     * @param dataSource a {@link DataSource}
     */
    private void mergePluginConfigurationParameter(PluginParameter pluginParam, DataSource dataSource) {
        if ((pluginParam.getPluginConfiguration() == null) || !pluginParam.getPluginConfiguration().getId()
                .equals(dataSource.getPluginConfigurationConnectionId())) {
            pluginParam.setPluginConfiguration(null);

            try {
                pluginParam.setPluginConfiguration(service
                        .getPluginConfiguration(dataSource.getPluginConfigurationConnectionId()));
            } catch (ModuleException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Converts a {@link PluginConfiguration} to a {@link DataSource}.</br>
     * @param pluginConf the {@link PluginConfiguration} to converts
     * @return the {@link DataSource} created
     * @throws IOException an error occurred when converts the JSon mapping to a {@link DataSourceModelMapping}.
     */
    private DataSource getDataSourceFromPluginConfiguration(PluginConfiguration pluginConf) throws IOException {
        DataSource dataSource = new DataSource();

        dataSource.setPluginConfigurationId(pluginConf.getId());
        dataSource.setLabel(pluginConf.getLabel());
        dataSource.setPluginClassName(pluginConf.getPluginClassName());
        dataSource.setFromClause(pluginConf.getStripParameterValue(IDBDataSourcePlugin.FROM_CLAUSE));
        dataSource.setTableName(pluginConf.getStripParameterValue(IDBDataSourceFromSingleTablePlugin.TABLE_PARAM));
        dataSource.setRefreshRate(Integer.parseInt(pluginConf.getParameterValue(IDataSourcePlugin.REFRESH_RATE)));

        String mapping = pluginConf.getParameterValue(IDBDataSourcePlugin.MODEL_PARAM);
        if (mapping != null) {
            dataSource.setMapping(adapter.fromJson(mapping));
        }

        PluginConfiguration plgConfig = pluginConf.getParameterConfiguration(IDBDataSourcePlugin.CONNECTION_PARAM);
        if (plgConfig != null) {
            dataSource.setPluginConfigurationConnectionId(plgConfig.getId());
        }

        return dataSource;
    }
}
