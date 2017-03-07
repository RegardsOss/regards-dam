/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameterType;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameterType.ParamType;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;

/**
 *
 * Unit testing of {@link DataSourceService}.
 *
 * @author Christophe Mertz
 */
public class DataSourceServiceTest {

    private IPluginService pluginServiceMock;

    /**
     * A mock of {@link IDBConnectionService}
     */
    private IDataSourceService dataSourceServiceMock;

    private IDBConnectionService connectionServiceMock;

    /**
     * A {@link List} of {@link PluginConfiguration}
     */
    private List<PluginConfiguration> plgConfs = new ArrayList<>();

    private PluginConfiguration internalConf;

    private PluginConfiguration externalConf;

    /**
     * This method is run before all tests
     */
    @Before
    public void init() {
        // create mock services
        pluginServiceMock = Mockito.mock(IPluginService.class);
        dataSourceServiceMock = new DataSourceService(pluginServiceMock, connectionServiceMock);

        // create PluginConfiguration
        internalConf = new PluginConfiguration(this.initializePluginMeta(), "internal configuration",
                initializePluginParameterIsInternalDataSource());
        externalConf = new PluginConfiguration(this.initializePluginMeta(), "external configuration",
                initializePluginParameterNotInternalDataSource());

        externalConf.setId(123456L);
        plgConfs.add(externalConf);
        plgConfs.add(new PluginConfiguration(this.initializePluginMeta(), "third configuration",
                initializePluginParameterNotInternalDataSource()));
        plgConfs.add(internalConf);
        plgConfs.add(new PluginConfiguration(this.initializePluginMeta(), "forth configuration",
                initializePluginParameterNotInternalDataSource()));
    }

    @Test
    public void setInternalDataSource() throws ModuleException {
        Assert.assertEquals(IDataSourcePlugin.TRUE_INTERNAL_DATASOURCE,
                            internalConf.getParameterValue(IDataSourcePlugin.IS_INTERNAL_PARAM));
        Assert.assertEquals("false",
                            externalConf.getParameterValue(IDataSourcePlugin.IS_INTERNAL_PARAM));
        
        Mockito.when(pluginServiceMock.getPluginConfiguration(externalConf.getId())).thenReturn(externalConf);
        Mockito.when(pluginServiceMock.getPluginConfigurationsByType(IDataSourcePlugin.class)).thenReturn(plgConfs);
        Mockito.when(pluginServiceMock.savePluginConfiguration(externalConf)).thenReturn(externalConf);
        PluginConfiguration newInternaleDS = dataSourceServiceMock.setInternalDataSource(externalConf);

        Assert.assertNotNull(newInternaleDS);
        Assert.assertEquals(newInternaleDS, externalConf);
        Assert.assertEquals(IDataSourcePlugin.TRUE_INTERNAL_DATASOURCE,
                            externalConf.getParameterValue(IDataSourcePlugin.IS_INTERNAL_PARAM));
        Assert.assertEquals("false",
                            internalConf.getParameterValue(IDataSourcePlugin.IS_INTERNAL_PARAM));
    }

    @Test
    public void getInternalDataSource() {
        Mockito.when(pluginServiceMock.getPluginConfigurationsByType(IDataSourcePlugin.class)).thenReturn(plgConfs);
        PluginConfiguration internaleDS = dataSourceServiceMock.getInternalDataSource();

        Assert.assertNotNull(internaleDS);
        Assert.assertEquals(internaleDS, internalConf);
    }

    private PluginMetaData initializePluginMeta() {
        final PluginMetaData pluginMetaData = new PluginMetaData();
        pluginMetaData.setPluginClassName(PostgreDataSourcePlugin.class.getCanonicalName());
        pluginMetaData.setPluginId("plugin-id");
        pluginMetaData.setAuthor("CS-SI");
        pluginMetaData.setVersion("1.0");
        pluginMetaData.setParameters(initializePluginParameterType());
        return pluginMetaData;
    }

    private List<PluginParameter> initializePluginParameterIsInternalDataSource() {
        return PluginParametersFactory.build().addParameter(IDataSourcePlugin.FROM_CLAUSE, "from t_table_name")
                .addParameter(IDataSourcePlugin.MODEL_PARAM, "model param")
                .addParameter(IDataSourcePlugin.IS_INTERNAL_PARAM, "true").getParameters();
    }

    private List<PluginParameter> initializePluginParameterNotInternalDataSource() {
        return PluginParametersFactory.build().addParameter(IDataSourcePlugin.FROM_CLAUSE, "from table")
                .addParameter(IDataSourcePlugin.MODEL_PARAM, "model")
                .addParameter(IDataSourcePlugin.IS_INTERNAL_PARAM, "false").getParameters();
    }

    private List<PluginParameterType> initializePluginParameterType() {
        return Arrays.asList(
                             new PluginParameterType(IDataSourcePlugin.MODEL_PARAM, String.class.getName(),
                                     ParamType.PRIMITIVE),
                             new PluginParameterType(IDataSourcePlugin.FROM_CLAUSE, String.class.getName(),
                                     ParamType.PRIMITIVE),
                             new PluginParameterType(IDataSourcePlugin.IS_INTERNAL_PARAM, String.class.getName(),
                                     ParamType.PRIMITIVE));
    }

}
