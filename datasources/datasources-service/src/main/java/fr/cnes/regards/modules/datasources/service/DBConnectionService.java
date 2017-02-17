/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.service;

import java.util.List;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.modules.plugins.service.PluginService;
import fr.cnes.regards.modules.datasources.domain.DBConnection;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;

/**
 * @author Christophe Mertz
 *
 */
@Service
public class DBConnectionService implements IDBConnectionService {

    /**
     * Attribute plugin service
     */
    private IPluginService service;

    /**
     * The constructor with an instance of the {@link PluginService}
     *
     * @param pPluginService
     *            The {@link PluginService} to used by this service
     */
    public DBConnectionService(IPluginService pPluginService) {
        super();
        this.service = pPluginService;
        this.service.addPluginPackage("fr.cnes.regards.modules.datasources.plugins");
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.service.IDBConnectionService#getAllDBConnections()
     */
    @Override
    public List<PluginConfiguration> getAllDBConnections() {
        return service.getPluginConfigurationsByType(IDBConnectionPlugin.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.service.IDBConnectionService#saveDBConnection(fr.cnes.regards.modules.
     * datasources.domain.DBConnection)
     */
    @Override
    public PluginConfiguration createDBConnection(DBConnection pDbConnection) throws ModuleException {
        PluginMetaData metaData = null;
        final List<PluginMetaData> metaDatas = service.getPluginsByType(IDBConnectionPlugin.class);

        // is the DbConnection match a PluginMetada
        boolean isFound = false;
        for (PluginMetaData pMd : metaDatas) {
            if (!isFound && pMd.getPluginClassName().equals(pDbConnection.getPluginClassName())) {
                isFound = true;
                metaData = pMd;
            }
        }
        if (!isFound) {
            throw new EntityInvalidException(
                    "The DBConnection contains an unknown plugin class name : " + pDbConnection.getPluginClassName());
        }

        return service.savePluginConfiguration(new PluginConfiguration(metaData, pDbConnection.getLabel(),
                buildParameters(pDbConnection), 0));
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.service.IDBConnectionService#getDBConnection(java.lang.Long)
     */
    @Override
    public PluginConfiguration getDBConnection(Long pId) throws ModuleException {
        return service.getPluginConfiguration(pId);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.service.IDBConnectionService#updateDBConnection(fr.cnes.regards.modules.
     * datasources.domain.DBConnection)
     */
    @Override
    public PluginConfiguration updateDBConnection(DBConnection pDbConnection) throws ModuleException {
        // Get the PluginConfiguration
        PluginConfiguration plgConf = service.getPluginConfiguration(pDbConnection.getPluginConfigurationId());
        plgConf.setLabel(pDbConnection.getLabel());

        // Update the PluginParamater of the PluginConfiguration
        UnaryOperator<PluginParameter> unaryOpt = pn -> mergeParameters(pn, pDbConnection);
        plgConf.getParameters().replaceAll(unaryOpt);

        return service.updatePluginConfiguration(plgConf);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.service.IDBConnectionService#deleteDBConnection(java.lang.Long)
     */
    @Override
    public void deleteDBConnection(Long pId) throws ModuleException {
        service.deletePluginConfiguration(pId);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.service.IDBConnectionService#testDBConnection(java.lang.Long)
     */
    @Override
    public Boolean testDBConnection(Long pId) throws ModuleException {
        IDBConnectionPlugin plg = service.getPlugin(pId);
        return plg.testConnection();
    }

    /**
     * Build a {@link List} of {@link PluginParameter} for the {@link IDBConnectionPlugin}.
     *
     * @param pDbConn
     * @return a {@link List} of {@link PluginParameter}
     */
    private List<PluginParameter> buildParameters(DBConnection pDbConn) {
        PluginParametersFactory factory = PluginParametersFactory.build();
        factory.addParameter(IDBConnectionPlugin.USER_PARAM, pDbConn.getUser())
                .addParameter(IDBConnectionPlugin.PASSWORD_PARAM, pDbConn.getPassword())
                .addParameter(IDBConnectionPlugin.URL_PARAM, pDbConn.getUrl())
                .addParameter(IDBConnectionPlugin.DRIVER_PARAM, pDbConn.getDriver())
                .addParameter(IDBConnectionPlugin.MAX_POOLSIZE_PARAM, pDbConn.getMaxPoolSize().toString())
                .addParameter(IDBConnectionPlugin.MIN_POOLSIZE_PARAM, pDbConn.getMinPoolSize().toString());

        return factory.getParameters();
    }

    /**
     * Update the {@link PluginParameter} with the appropriate {@link DBConnection} attribute
     *
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
            case IDBConnectionPlugin.URL_PARAM:
                pPlgParam.setValue(pDbConn.getUrl());
                break;
            case IDBConnectionPlugin.DRIVER_PARAM:
                pPlgParam.setValue(pDbConn.getDriver());
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
}
