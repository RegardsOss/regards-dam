package fr.cnes.regards.modules.dam.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.module.manager.AbstractModuleManager;
import fr.cnes.regards.framework.module.manager.ModuleConfiguration;
import fr.cnes.regards.framework.module.manager.ModuleConfigurationItem;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IConnectionPlugin;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDataSourcePlugin;
import fr.cnes.regards.modules.dam.service.datasources.IDBConnectionService;
import fr.cnes.regards.modules.dam.service.datasources.IDataSourceService;

/**
 * DAM configuration manager. Exports model & connection plugin configurations & datasource plugin configurations.
 *
 * @author Sylvain VISSIERE-GUERINET
 */
@Component
public class DamConfigurationManager extends AbstractModuleManager<Void> {

    public static final String PLUGIN_CONFIGURATION_ALREADY_EXISTS = "A plugin configuration already exists with same label, skipping import of %s.";

    public static final String VALIDATION_ISSUES = "Skipping import of %s for these reasons: %s";

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IDBConnectionService connectionService;

    @Autowired
    private IDataSourceService dataSourceService;

    @Override
    protected Set<String> importConfiguration(ModuleConfiguration configuration) {
        Set<String> importErrors = new HashSet<>();
        for (ModuleConfigurationItem<?> item : configuration.getConfiguration()) {
            // Lets import connection and data sources
            if (PluginConfiguration.class.isAssignableFrom(item.getKey())) {
                PluginConfiguration plgConf = item.getTypedValue();
                if (pluginService.findPluginConfigurationByLabel(plgConf.getLabel()).isPresent()) {
                    importErrors.add(String.format(PLUGIN_CONFIGURATION_ALREADY_EXISTS, plgConf.getLabel()));
                } else {
                    EntityInvalidException validationIssues = PluginUtils.validate(plgConf);
                    if (validationIssues == null) {
                        // Now that we are about to create the plugin configuration, lets check for IDataStorage
                        if (plgConf.getInterfaceNames().contains(IConnectionPlugin.class.getName())) {
                            try {
                                connectionService.createDBConnection(plgConf);
                            } catch (ModuleException e) {
                                importErrors.add(String.format("Skipping import of Data Storage %s: %s",
                                                               plgConf.getLabel(), e.getMessage()));
                                logger.error(e.getMessage(), e);
                            }
                        } else {
                            if (plgConf.getInterfaceNames().contains(IDataSourcePlugin.class.getName())) {
                                try {
                                    dataSourceService.createDataSource(plgConf);
                                } catch (ModuleException e) {
                                    // This should not occurs, but we never know
                                    importErrors.add(String.format("Skipping import of PluginConfiguration %s: %s",
                                                                   plgConf.getLabel(), e.getMessage()));
                                    logger.error(e.getMessage(), e);
                                }
                            }
                        }
                    } else {
                        importErrors.add(String.format(VALIDATION_ISSUES, plgConf.getLabel(), validationIssues
                                .getMessages().stream().collect(Collectors.joining(",", "", "."))));
                    }
                }
            }
        }
        return importErrors;
    }

    @Override
    public ModuleConfiguration exportConfiguration() throws ModuleException {
        List<ModuleConfigurationItem<?>> configurations = new ArrayList<>();
        // export connections
        for (PluginConfiguration connection : pluginService.getPluginConfigurationsByType(IConnectionPlugin.class)) {
            configurations.add(ModuleConfigurationItem.build(connection));
        }
        // export datasources
        for (PluginConfiguration connection : pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class)) {
            configurations.add(ModuleConfigurationItem.build(connection));
        }
        return ModuleConfiguration.build(info, configurations);
    }
}
