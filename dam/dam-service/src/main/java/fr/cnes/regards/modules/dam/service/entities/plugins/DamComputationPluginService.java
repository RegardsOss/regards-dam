/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
 */
package fr.cnes.regards.modules.dam.service.entities.plugins;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.IPluginParam;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.dam.plugin.entities.AbstractDataObjectComputePlugin;
import fr.cnes.regards.modules.dam.plugin.entities.CountPlugin;
import fr.cnes.regards.modules.dam.plugin.entities.IntSumComputePlugin;
import fr.cnes.regards.modules.dam.plugin.entities.LongSumComputePlugin;
import fr.cnes.regards.modules.dam.plugin.entities.MaxDateComputePlugin;
import fr.cnes.regards.modules.dam.plugin.entities.MinDateComputePlugin;
import fr.cnes.regards.modules.dam.service.models.IAttributeModelService;
import fr.cnes.regards.modules.dam.service.models.exception.ImportException;
import fr.cnes.regards.modules.dam.service.models.xml.IComputationPluginService;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.Fragment;
import fr.cnes.regards.modules.model.domain.schema.Attribute;
import fr.cnes.regards.modules.model.domain.schema.Computation;
import fr.cnes.regards.modules.model.domain.schema.ParamPluginType;

/**
 * Initialize computation plugin for DAM module
 * @author Marc SORDI
 */
@Service
public class DamComputationPluginService implements IComputationPluginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DamComputationPluginService.class);

    @Autowired
    private IAttributeModelService attModelService;

    @Override
    public PluginConfiguration getPlugin(Attribute xmlAtt) throws ImportException {

        Computation computation = xmlAtt.getComputation();

        // Target plugin class
        Class<?> pluginClass = null;
        // If compute plugin is of type paramPluginType, parameters should be added as PluginParameter
        ParamPluginType xmlParamPluginType = null;

        if (computation.getCount() != null) {
            pluginClass = CountPlugin.class;
        } else if (computation.getSumCompute() != null) {
            xmlParamPluginType = computation.getSumCompute();

            AttributeModel att = findAttribute(xmlParamPluginType.getParameterAttributeFragmentName(),
                                               xmlParamPluginType.getParameterAttributeName());
            // Depends on attribute type
            switch (att.getType()) {
                case INTEGER:
                    pluginClass = IntSumComputePlugin.class;
                    break;
                case LONG:
                    pluginClass = LongSumComputePlugin.class;
                    break;
                default:
                    String message = String
                            .format("Only LONG and INTEGER attribute types are supported for sum_compute plugin"
                                    + " (attribute %s with type %s)", xmlAtt.getName(), xmlAtt.getType());
                    LOGGER.error(message);
                    throw new ImportException(message);
            }
        } else if (computation.getMinCompute() != null) {
            xmlParamPluginType = computation.getMinCompute();

            AttributeModel att = findAttribute(xmlParamPluginType.getParameterAttributeFragmentName(),
                                               xmlParamPluginType.getParameterAttributeName());
            // Depends on attribute type
            switch (att.getType()) {
                case DATE_ISO8601:
                    pluginClass = MinDateComputePlugin.class;
                    break;
                default:
                    String message = String.format(
                                                   "Only DATE attribute types are supported for min_compute plugin"
                                                           + " (attribute %s with type %s)",
                                                   xmlAtt.getName(), xmlAtt.getType());
                    LOGGER.error(message);
                    throw new ImportException(message);
            }
        } else if (computation.getMaxCompute() != null) {
            xmlParamPluginType = computation.getMaxCompute();

            AttributeModel att = findAttribute(xmlParamPluginType.getParameterAttributeFragmentName(),
                                               xmlParamPluginType.getParameterAttributeName());
            // Depends on attribute type
            switch (att.getType()) {
                case DATE_ISO8601:
                    pluginClass = MaxDateComputePlugin.class;
                    break;
                default:
                    String message = String.format(
                                                   "Only DATE attribute types are supported for max_compute plugin"
                                                           + " (attribute %s with type %s)",
                                                   xmlAtt.getName(), xmlAtt.getType());
                    LOGGER.error(message);
                    throw new ImportException(message);
            }
        } else {
            String message = String.format("Unknown compute plugin for attribute %s", xmlAtt.getName());
            LOGGER.error(message);
            throw new ImportException(message);
        }
        return createPluginConfiguration(xmlAtt, pluginClass, xmlParamPluginType);
    }

    private AttributeModel findAttribute(String fragment, String name) throws ImportException {
        AttributeModel attModel = attModelService
                .findByNameAndFragmentName(name, fragment != null ? fragment : Fragment.getDefaultName());
        if (attModel == null) {
            String message = String.format("Unknown attribute with name %s and fragment %s", name,
                                           fragment != null ? fragment : Fragment.getDefaultName());
            LOGGER.error(message);
            throw new ImportException(message);
        }
        return attModel;
    }

    private PluginConfiguration createPluginConfiguration(Attribute xmlAtt, Class<?> pluginClass,
            ParamPluginType xmlParamPluginType) throws ImportException {

        PluginMetaData plgMetaData = PluginUtils.createPluginMetaData(pluginClass);
        PluginConfiguration compConf = new PluginConfiguration(plgMetaData, xmlAtt.getComputation().getLabel());
        // Add plugin parameters (from attribute and associated fragment)
        Set<IPluginParam> parameters = IPluginParam.set();
        // Some plugins need parameters (in this case, xmlParamPluginType contains them as attributes)
        if (xmlParamPluginType != null) {
            parameters.add(IPluginParam.build(AbstractDataObjectComputePlugin.PARAMETER_ATTRIBUTE_NAME,
                                              xmlParamPluginType.getParameterAttributeName()));
            // attribute fragment name being an optional parameter, lets check it
            if (xmlParamPluginType.getParameterAttributeFragmentName() != null) {
                parameters.add(IPluginParam.build(AbstractDataObjectComputePlugin.PARAMETER_FRAGMENT_NAME,
                                                  xmlParamPluginType.getParameterAttributeFragmentName()));
            }
        }
        compConf.setParameters(parameters);
        return compConf;
    }
}
