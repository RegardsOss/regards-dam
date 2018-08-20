package fr.cnes.regards.modules.dam.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.module.manager.AbstractModuleConfigurationManager;
import fr.cnes.regards.framework.module.manager.ModuleConfiguration;
import fr.cnes.regards.framework.module.manager.ModuleConfigurationItem;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IConnectionPlugin;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDataSourcePlugin;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.service.datasources.IDBConnectionService;
import fr.cnes.regards.modules.dam.service.datasources.IDataSourceService;
import fr.cnes.regards.modules.dam.service.models.IAttributeModelService;
import fr.cnes.regards.modules.dam.service.models.IModelAttrAssocService;
import fr.cnes.regards.modules.dam.service.models.IModelService;
import fr.cnes.regards.modules.dam.service.models.exception.FragmentAttributeException;

/**
 * DAM configuration manager. Exports model & connection plugin configurations & datasource plugin configurations.
 *
 * @author Sylvain VISSIERE-GUERINET
 */
@Component
public class DamConfigurationManager extends AbstractModuleConfigurationManager {

    public static final String PLUGIN_CONFIGURATION_ALREADY_EXISTS = "A plugin configuration already exists with same label, skipping import of %s.";

    public static final String VALIDATION_ISSUES = "Skipping import of %s for these reasons: %s";

    @Autowired
    private IModelService modelService;

    @Autowired
    private IAttributeModelService attributeModelService;

    @Autowired
    private IModelAttrAssocService modelAttrAssocService;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IDBConnectionService connectionService;

    @Autowired
    private IDataSourceService dataSourceService;

    @Override
    protected Set<String> importConfiguration(ModuleConfiguration configuration) {
        Set<String> importErrors = new HashSet<>();
        // As export are done thanks to a list, order should be kept so unique for loop should be sufficient,
        // in case of issues, try to split into multiple for loop
        for (ModuleConfigurationItem<?> item : configuration.getConfiguration()) {
            // First lets import models
            if (Model.class.isAssignableFrom(item.getKey())) {
                Model model = item.getTypedValue();
                try {
                    modelService.createModel(model);
                } catch (ModuleException e) {
                    importErrors.add(String.format("Skipping import of Model %s: %s", model.getName(), e.getMessage()));
                    logger.error(e.getMessage(), e);
                }
            }
            // Second attribute
            if (AttributeModel.class.isAssignableFrom(item.getKey())) {
                AttributeModel attribute = item.getTypedValue();
                try {
                    attributeModelService.createAttribute(attribute);
                } catch (ModuleException e) {
                    importErrors.add(String.format("Skipping import of AttributeModel %s: %s",
                                                   attribute.getFullName(),
                                                   e.getMessage()));
                    logger.error(e.getMessage(), e);
                }
            }
            // third associations
            if (ModelAttrAssoc.class.isAssignableFrom(item.getKey())) {
                ModelAttrAssoc assoc = item.getTypedValue();
                // here is the tricky part: lets try to create the association
                // in case of EntityAlreadyExistsException, lets log import has been skipped
                // in case of FragmentAttributeException, lets bind the fragment
                // first, lets get the attribute from DB to get the id
                assoc.setAttribute(attributeModelService.findByNameAndFragmentName(assoc.getAttribute().getName(),
                                                                                   assoc.getAttribute().getFragment()
                                                                                           == null ?
                                                                                           null :
                                                                                           assoc.getAttribute()
                                                                                                   .getFragment()
                                                                                                   .getName()));
                try {
                    modelAttrAssocService.bindAttributeToModel(assoc.getModel().getName(), assoc);
                } catch (EntityAlreadyExistsException e) {
                    // no rethrow or log of exception because we know what happened
                    importErrors.add(String.format(
                            "Association between model %s and attribute %s already exists, skipping import.",
                            assoc.getModel().getName(),
                            assoc.getAttribute().getFullName()));
                } catch (FragmentAttributeException e1) {
                    // association reflects association between a model and a fragment so lets try to bind the fragment
                    try {
                        modelAttrAssocService
                                .bindNSAttributeToModel(assoc.getModel().getName(), assoc.getAttribute().getFragment());
                    } catch (ModuleException e) {
                        importErrors.add(String.format(
                                "Skipping import of association between model %s and fragment %s: %s",
                                assoc.getModel().getName(),
                                assoc.getAttribute().getFragment().getName(),
                                e.getMessage()));
                        logger.error(e.getMessage(), e);
                    }
                } catch (ModuleException e) {
                    importErrors.add(String.format(
                            "Skipping import of association between model %s and attribute %s: %s",
                            assoc.getModel().getName(),
                            assoc.getAttribute().getFullName(),
                            e.getMessage()));
                    logger.error(e.getMessage(), e);
                }
            }
            // Now lets import connection and data sources
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
                                                               plgConf.getLabel(),
                                                               e.getMessage()));
                                logger.error(e.getMessage(), e);
                            }
                        } else {
                            if (plgConf.getInterfaceNames().contains(IDataSourcePlugin.class.getName())) {
                                try {
                                    dataSourceService.createDataSource(plgConf);
                                } catch (ModuleException e) {
                                    // This should not occurs, but we never know
                                    importErrors.add(String.format("Skipping import of PluginConfiguration %s: %s",
                                                                   plgConf.getLabel(),
                                                                   e.getMessage()));
                                    logger.error(e.getMessage(), e);
                                }
                            }
                        }
                    } else {
                        importErrors.add(String.format(VALIDATION_ISSUES,
                                                       plgConf.getLabel(),
                                                       validationIssues.getMessages().stream()
                                                               .collect(Collectors.joining(",", "", "."))));
                    }
                }
            }
        }
        return importErrors;
    }

    @Override
    public ModuleConfiguration exportConfiguration() throws ModuleException {
        List<ModuleConfigurationItem<?>> configurations = new ArrayList<>();
        // exporting dam configuration must be done in a particular order because of import constraints:
        // models->attributes->modelAttrAssocs->connection conf->data source conf
        // export models
        for (Model model : modelService.getModels(null)) {
            configurations.add(ModuleConfigurationItem.build(model));
        }
        // export attributes
        for (AttributeModel attr : attributeModelService.getAttributes(null, null, null)) {
            configurations.add(ModuleConfigurationItem.build(attr));
        }
        //export modelAttrAssocs
        for (ModelAttrAssoc modelAttrAssoc : modelAttrAssocService.getModelAttrAssocsFor(null)) {
            configurations.add(ModuleConfigurationItem.build(modelAttrAssoc));
        }
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
