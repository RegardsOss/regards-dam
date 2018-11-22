package fr.cnes.regards.modules.dam.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

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
        Pair<Set<PluginConfiguration>, Set<PluginConfiguration>> confs = dispatch(getPluginConfs(configuration
                .getConfiguration()));

        // First create connections
        for (PluginConfiguration plgConf : confs.getLeft()) {
            importErrors.addAll(createConnection(plgConf));
        }

        // Then create datasources
        for (PluginConfiguration plgConf : confs.getRight()) {
            importErrors.addAll(createDatasource(plgConf));
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

    private Set<String> createConnection(PluginConfiguration plgConf) {
        Set<String> importErrors = new HashSet<>();
        if (pluginService.findPluginConfigurationByLabel(plgConf.getLabel()).isPresent()) {
            importErrors.add(String.format(PLUGIN_CONFIGURATION_ALREADY_EXISTS, plgConf.getLabel()));
        } else {
            EntityInvalidException validationIssues = PluginUtils.validate(plgConf);
            if (validationIssues == null) {
                try {
                    connectionService.createDBConnection(plgConf);
                } catch (ModuleException e) {
                    importErrors.add(String.format("Skipping import of Data Storage %s: %s", plgConf.getLabel(),
                                                   e.getMessage()));
                    logger.error(e.getMessage(), e);
                }
            } else {
                importErrors.add(String
                        .format(VALIDATION_ISSUES, plgConf.getLabel(),
                                validationIssues.getMessages().stream().collect(Collectors.joining(",", "", "."))));
            }

        }
        return importErrors;
    }

    private Set<String> createDatasource(PluginConfiguration plgConf) {
        Set<String> importErrors = new HashSet<>();
        if (pluginService.findPluginConfigurationByLabel(plgConf.getLabel()).isPresent()) {
            importErrors.add(String.format(PLUGIN_CONFIGURATION_ALREADY_EXISTS, plgConf.getLabel()));
        } else {
            EntityInvalidException validationIssues = PluginUtils.validate(plgConf);
            if (validationIssues == null) {
                try {
                    dataSourceService.createDataSource(plgConf);
                } catch (ModuleException e) {
                    // This should not occurs, but we never know
                    importErrors.add(String.format("Skipping import of PluginConfiguration %s: %s", plgConf.getLabel(),
                                                   e.getMessage()));
                    logger.error(e.getMessage(), e);
                }
            } else {
                importErrors.add(String
                        .format(VALIDATION_ISSUES, plgConf.getLabel(),
                                validationIssues.getMessages().stream().collect(Collectors.joining(",", "", "."))));
            }

        }
        return importErrors;
    }

    private Set<PluginConfiguration> getPluginConfs(Collection<ModuleConfigurationItem<?>> items) {
        return items.stream().filter(i -> PluginConfiguration.class.isAssignableFrom(i.getKey()))
                .map(i -> (PluginConfiguration) i.getTypedValue()).collect(Collectors.toSet());
    }

    private Pair<Set<PluginConfiguration>, Set<PluginConfiguration>> dispatch(Collection<PluginConfiguration> confs) {
        Set<PluginConfiguration> connections = Sets.newHashSet();
        Set<PluginConfiguration> datasources = Sets.newHashSet();
        confs.forEach(c -> {
            if (c.getInterfaceNames().contains(IConnectionPlugin.class.getName())) {
                connections.add(c);
            } else if (c.getInterfaceNames().contains(IDataSourcePlugin.class.getName())) {
                datasources.add(c);
            }
        });
        return Pair.of(connections, datasources);
    }
}
