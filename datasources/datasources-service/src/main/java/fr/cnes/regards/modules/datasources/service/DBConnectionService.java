/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.service;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.modules.plugins.service.PluginService;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.DBConnection;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;

/**
 * @author Christophe Mertz
 *
 */
@Service
@MultitenantTransactional
public class DBConnectionService implements IDBConnectionService {

    /**
     * Attribute plugin service
     */
    private final IPluginService pluginService;

    /**
     * The constructor with an instance of the {@link PluginService}
     *
     * @param pPluginService
     *            The {@link PluginService} to used by this service
     */
    public DBConnectionService(IPluginService pPluginService) {
        super();
        this.pluginService = pPluginService;
        this.pluginService.addPluginPackage(DefaultPostgreConnectionPlugin.class.getPackage().getName());
    }

    @Override
    public List<PluginConfiguration> getAllDBConnections() {
        return pluginService.getPluginConfigurationsByType(IDBConnectionPlugin.class);
    }

    @Override
    public PluginConfiguration createDBConnection(DBConnection pDbConnection) throws ModuleException {

        PluginMetaData metaData = pluginService.checkPluginClassName(IDBConnectionPlugin.class,
                                                                     pDbConnection.getPluginClassName());

        return pluginService.savePluginConfiguration(new PluginConfiguration(metaData, pDbConnection.getLabel(),
                buildParameters(pDbConnection)));
    }

    @Override
    public PluginConfiguration getDBConnection(Long pConfigurationId) throws ModuleException {
        return pluginService.getPluginConfiguration(pConfigurationId);
    }

    @Override
    public PluginConfiguration updateDBConnection(DBConnection pDbConnection) throws ModuleException {
        // Get the PluginConfiguration
        PluginConfiguration plgConf = pluginService.getPluginConfiguration(pDbConnection.getPluginConfigurationId());

        // Update the PluginParamater of the PluginConfiguration
        UnaryOperator<PluginParameter> unaryOpt = pn -> mergeParameters(pn, pDbConnection);
        plgConf.getParameters().replaceAll(unaryOpt);

        return pluginService.updatePluginConfiguration(plgConf);
    }

    @Override
    public void deleteDBConnection(Long pConfigurationId) throws ModuleException {
        pluginService.deletePluginConfiguration(pConfigurationId);
    }

    @Override
    public Boolean testDBConnection(Long pConfigurationId) throws ModuleException {
        // Instanciate plugin
        IDBConnectionPlugin plg = pluginService.getPlugin(pConfigurationId);
        // Test connection
        Boolean result = plg.testConnection();
        // Remove plugin instance from cache after closing connection
        if (!result) {
            pluginService.cleanPluginCache(pConfigurationId);
        }
        return result;
    }

    /**
     * Build a {@link List} of {@link PluginParameter} for the {@link IDBConnectionPlugin}.
     *
     * @param pDbConn
     *            A {@link DBConnection}
     * @return a {@link List} of {@link PluginParameter}
     */
    private List<PluginParameter> buildParameters(DBConnection pDbConn) {
        PluginParametersFactory factory = PluginParametersFactory.build();
        factory.addParameter(IDBConnectionPlugin.USER_PARAM, pDbConn.getUser())
                .addParameter(IDBConnectionPlugin.PASSWORD_PARAM, pDbConn.getPassword())
                .addParameter(IDBConnectionPlugin.DB_HOST_PARAM, pDbConn.getDbHost())
                .addParameter(IDBConnectionPlugin.DB_PORT_PARAM, pDbConn.getDbPort())
                .addParameter(IDBConnectionPlugin.DB_NAME_PARAM, pDbConn.getDbName())
                .addParameter(IDBConnectionPlugin.MAX_POOLSIZE_PARAM, pDbConn.getMaxPoolSize().toString())
                .addParameter(IDBConnectionPlugin.MIN_POOLSIZE_PARAM, pDbConn.getMinPoolSize().toString());

        return factory.getParameters();
    }

    /**
     * Update the {@link PluginParameter} with the appropriate {@link DBConnection} attribute
     *
     * @param pPlgParam
     *            a {@link PluginParameter}
     * @param pDbConn
     *            A {@link DBConnection}
     * @return a {{@link PluginParameter}
     */
    private PluginParameter mergeParameters(PluginParameter pPlgParam, DBConnection pDbConn) {
        switch (pPlgParam.getName()) {
            case IDBConnectionPlugin.USER_PARAM:
                pPlgParam.setValue(pDbConn.getUser());
                break;
            case IDBConnectionPlugin.PASSWORD_PARAM:
                pPlgParam.setValue(pDbConn.getPassword());
                break;
            case IDBConnectionPlugin.DB_HOST_PARAM:
                pPlgParam.setValue(pDbConn.getDbHost());
                break;
            case IDBConnectionPlugin.DB_PORT_PARAM:
                pPlgParam.setValue(pDbConn.getDbPort());
                break;
            case IDBConnectionPlugin.DB_NAME_PARAM:
                pPlgParam.setValue(pDbConn.getDbName());
                break;
            case IDBConnectionPlugin.MIN_POOLSIZE_PARAM:
                pPlgParam.setValue(pDbConn.getMinPoolSize().toString());
                break;
            case IDBConnectionPlugin.MAX_POOLSIZE_PARAM:
                pPlgParam.setValue(pDbConn.getMaxPoolSize().toString());
                break;
            default:
                break;
        }
        return pPlgParam;
    }

    @Override
    public Map<String, Table> getTables(Long pId) throws ModuleException {
        IDBConnectionPlugin plg = pluginService.getPlugin(pId);
        return plg.getTables();
    }

    @Override
    public Map<String, Column> getColumns(Long pId, String pTableName) throws ModuleException {
        IDBConnectionPlugin plg = pluginService.getPlugin(pId);
        return plg.getColumns(pTableName);
    }

}
