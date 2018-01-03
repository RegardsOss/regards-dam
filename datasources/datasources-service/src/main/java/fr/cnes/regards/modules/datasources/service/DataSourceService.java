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
import java.util.List;

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
 * DataSource specific plugin service façade implementation
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 * @author oroussel
 */
@Service
@MultitenantTransactional
public class DataSourceService implements IDataSourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceService.class);

    // TODO VIRER tout ce qui doit être autowired du constructeur

    private final IPluginService service;

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
    public List<PluginConfiguration> getAllDataSources() {
        return service.getPluginConfigurationsByType(IDataSourcePlugin.class);
    }

    @Override
    public PluginConfiguration createDataSource(PluginConfiguration dataSource) throws ModuleException {
        return service.savePluginConfiguration(dataSource);

/*        try {
            String plgClassName = dataSource.getPluginClassName();
            Class pluginClass = Class.forName(plgClassName);

            if (Stream.of(pluginClass.getInterfaces()).anyMatch(c -> c.equals(IDBDataSourcePlugin.class))) {
                if (Stream.of(pluginClass.getInterfaces()).anyMatch(c -> c.equals(IDBDataSourceFromSingleTablePlugin.class))) {
                    //            if ((dataSource.getTableName() != null) && (dataSource.getFromClause() == null)) {
//                    LOGGER.info("table name : " + dataSource.getTableName());
                    LOGGER.info("table name : " + dataSource.get(IDBDataSourceFromSingleTablePlugin.TABLE_PARAM));
                    return getDataSourceFromPluginConfiguration(createDataSourceFromSingleTable(dataSource));
                } else {
//                    LOGGER.info("from clause : " + dataSource.getFromClause());
                    LOGGER.info("from clause : " + dataSource.get(IDBDataSourcePlugin.FROM_CLAUSE));
                    return getDataSourceFromPluginConfiguration(createDataSourceFromComplexRequest(dataSource));
                }
            } else if (Stream.of(pluginClass.getInterfaces()).anyMatch(c -> c.equals(IAipDataSourcePlugin.class))) {
                // TODO manage Aip datasource
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ModuleException("Unable to converts a PluginConfiguration to a Datasourceobject");
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ModuleException("Unknown plugin");
        }

        ModuleException ex = new ModuleException("The incoming datasource is inconsistent");
        LOGGER.error(ex.getMessage());
        throw ex;*/
    }

    /**
     * Create a {@link PluginConfiguration} for a plugin's type {@link IDBDataSourceFromSingleTablePlugin} with a
     * {@link DataSource}.
     * @param dataSource the {@link DataSource} to used to create the {@link PluginConfiguration}
     * @return the {@link PluginConfiguration} created
     */
    private PluginConfiguration createDataSourceFromSingleTable(DataSource dataSource) throws ModuleException {
        LOGGER.info("createDataSource : " + dataSource.getLabel());

        PluginMetaData metaData = service
                .checkPluginClassName(IDBDataSourceFromSingleTablePlugin.class, dataSource.getPluginClassName());
        return service.savePluginConfiguration(
                new PluginConfiguration(metaData, dataSource.getLabel(), buildParametersSingleTable(dataSource)));
    }

    /**
     * Create a {@link PluginConfiguration} for a plugin's type {@link IDataSourcePlugin} with a {@link DataSource}
     * @param dataSource the {@link DataSource} to used to create the {@link PluginConfiguration}
     * @return the {@link PluginConfiguration} created
     */
    private PluginConfiguration createDataSourceFromComplexRequest(DataSource dataSource) throws ModuleException {
        LOGGER.info("createDataSource : " + dataSource.getLabel());

        PluginMetaData metaData = service
                .checkPluginClassName(IDataSourcePlugin.class, dataSource.getPluginClassName());
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
        factory.addPluginConfiguration(IDBDataSourcePlugin.CONNECTION_PARAM, dbConnectionService
                .getDBConnection(dataSource.getPluginConfigurationConnectionId()))
                .addParameter(IDBDataSourcePlugin.MODEL_PARAM, dataSource.getMapping())
                .addParameter(IDataSourcePlugin.REFRESH_RATE, (dataSource.getRefreshRate() == null) ?
                        IDataSourcePlugin.REFRESH_RATE_DEFAULT_VALUE :
                        dataSource.getRefreshRate());

        return factory;
    }

    /**
     * Create a {@link List} of {@link PluginParameter} for a plugin's type type
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
    public PluginConfiguration getDataSource(Long id) throws EntityNotFoundException {
        return service.getPluginConfiguration(id);
/*        try {
            return getDataSourceFromPluginConfiguration(service.getPluginConfiguration(id));
        } catch (ModuleException e) {
            LOGGER.error("No plugin configuration found for id:" + id, e);
            throw new EntityNotFoundException(e.getMessage(), PluginConfiguration.class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new EntityNotFoundException("Unable to converts a PluginConfiguration to a Datasource object",
                    PluginConfiguration.class);
        }*/
    }

    @Override
    public PluginConfiguration updateDataSource(PluginConfiguration dataSource) throws ModuleException {
        LOGGER.info("updateDataSource : id = {}, [new] label = {}", dataSource.getId(), dataSource.getLabel());

        // Get current datasource PluginConfiguration
        PluginConfiguration dataSourceFromDb = service.getPluginConfiguration(dataSource.getId());

        // Manage the label change
        dataSourceFromDb.setLabel(dataSource.getLabel());

        // Manage the change between a DataSource from a single table and a from clause
        // FIXME on ne peut pas changer le type de plugin en cours de route donc ce cas n'arrive pas (à vérifier quand même)
/*        PluginParameter paramTableName = dataSourceFromDb.getParameter(IDBDataSourceFromSingleTablePlugin.TABLE_PARAM);
        if ((paramTableName != null) && (dataSource.getFromClause() != null) && !""
                .equals(dataSource.getFromClause())) {
            dataSourceFromDb.setParameters(PluginParametersFactory.build(dataSourceFromDb.getParameters()).removeParameter(paramTableName)
                                          .addPluginConfiguration(IDBDataSourcePlugin.FROM_CLAUSE, null)
                                          .getParameters());
        } else {
            PluginParameter paramFromClause = dataSourceFromDb.getParameter(IDBDataSourcePlugin.FROM_CLAUSE);
            if ((paramFromClause != null) && (dataSource.getTableName() != null) && !""
                    .equals(dataSource.getTableName())) {
                dataSourceFromDb.setParameters(
                        PluginParametersFactory.build(dataSourceFromDb.getParameters()).removeParameter(paramFromClause)
                                .addPluginConfiguration(IDBDataSourceFromSingleTablePlugin.TABLE_PARAM, null)
                                .getParameters());
            }
        }*/

        // Update all PluginParameters
        dataSourceFromDb.getParameters().replaceAll(param -> mergeParameter(param, dataSource));
        return service.updatePluginConfiguration(dataSourceFromDb);
    }

    @Override
    public void deleteDataSouce(Long id) throws ModuleException {
        LOGGER.info("deleteDataSouce : " + id);

        service.deletePluginConfiguration(id);
    }

    /**
     * Update the {@link PluginParameter} with the appropriate {@link PluginConfiguration} datasource attribute
     * @param pluginParam a {@link PluginParameter} to update
     * @param dataSource a {@link DataSource}
     * @return a {{@link PluginParameter}
     */
    private PluginParameter mergeParameter(PluginParameter pluginParam, PluginConfiguration dataSource) {
        switch (pluginParam.getName()) {
            case IDBDataSourcePlugin.CONNECTION_PARAM:
                mergePluginConfigurationParameter(pluginParam, dataSource);
                break;
            // FIXME vérifier que dans le cas où le refreshRate devient null, la PluginConf, lorsqu'elle sera
            // rechargée, utilisera bien la valeur par défaut spécifiée sur l'annotation
            //            case IDataSourcePlugin.REFRESH_RATE:
            //                String refreshRate = dataSource.getParameterValue(IDataSourcePlugin.REFRESH_RATE);
            //                PluginParametersFactory.updateParameter(pluginParam, refreshRate == null ?
            //                        IDataSourcePlugin.REFRESH_RATE_DEFAULT_VALUE_AS_STRING :
            //                        refreshRate);
            //                break;
            default:
                // BEWARE : DataSource comes from frontend, its value is already gson-normalized SO don't use
                // PluginParametersFactory.updateParameter(...) method
                pluginParam.setValue(dataSource.getParameterValue(pluginParam.getName()));
                break;
        }
        return pluginParam;
    }

    /**
     * Update a {@link PluginParameter} of type connection
     * @param connectionPluginParam a {@link PluginParameter} to update
     * @param dataSource a {@link PluginConfiguration}
     */
    private void mergePluginConfigurationParameter(PluginParameter connectionPluginParam, PluginConfiguration dataSource) {
        PluginConfiguration dbConf = connectionPluginParam.getPluginConfiguration();
        PluginConfiguration currentDbConf = dataSource.getParameterConfiguration(IDBDataSourcePlugin.CONNECTION_PARAM);
        if ((dbConf == null) || !dbConf.getId().toString().equals(currentDbConf.getId())) {
            connectionPluginParam.setPluginConfiguration(currentDbConf);
        }
    }

    /**
     * Converts a {@link PluginConfiguration} to a {@link DataSource}.</br>
     * @param pluginConf the {@link PluginConfiguration} to converts
     * @return the {@link DataSource} created
     * @throws IOException an error occurred when converts the JSon mapping to a {@link DataSourceModelMapping}.
     */
    @Deprecated
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
