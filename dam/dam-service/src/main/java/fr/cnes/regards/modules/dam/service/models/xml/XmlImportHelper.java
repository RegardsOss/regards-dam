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
package fr.cnes.regards.modules.dam.service.models.xml;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.schema.Attribute;
import fr.cnes.regards.modules.dam.domain.models.schema.Computation;
import fr.cnes.regards.modules.dam.domain.models.schema.Fragment;
import fr.cnes.regards.modules.dam.domain.models.schema.Model;
import fr.cnes.regards.modules.dam.domain.models.schema.ParamPluginType;
import fr.cnes.regards.modules.dam.plugin.entities.CountPlugin;
import fr.cnes.regards.modules.dam.plugin.entities.IntSumComputePlugin;
import fr.cnes.regards.modules.dam.plugin.entities.LongSumComputePlugin;
import fr.cnes.regards.modules.dam.plugin.entities.MaxDateComputePlugin;
import fr.cnes.regards.modules.dam.plugin.entities.MinDateComputePlugin;
import fr.cnes.regards.modules.dam.service.models.exception.ImportException;

/**
 * Help to manage model XML import based on XML schema definition
 * @author Marc Sordi
 */
public final class XmlImportHelper {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlImportHelper.class);

    private XmlImportHelper() {
    }

    /**
     * Import fragment {@link AttributeModel} from input stream
     * @param pInputStream input stream
     * @return list of {@link AttributeModel} linked to same {@link Fragment}
     * @throws ImportException if error occurs!
     */
    public static List<AttributeModel> importFragment(InputStream pInputStream) throws ImportException {
        final Fragment xmlFragment = read(pInputStream, Fragment.class);

        if (xmlFragment.getAttribute().isEmpty()) {
            final String message = String
                    .format("Import for fragment %s is skipped because no attribute is bound!", xmlFragment.getName());
            LOGGER.error(message);
            throw new ImportException(message);
        }

        final List<AttributeModel> attModels = new ArrayList<>();

        // Manage fragment
        // CHECKSTYLE:OFF
        fr.cnes.regards.modules.dam.domain.models.attributes.Fragment fragment = new fr.cnes.regards.modules.dam.domain.models.attributes.Fragment();
        // CHECKSTYLE:ON
        fragment.fromXml(xmlFragment);

        for (Attribute xmlAtt : xmlFragment.getAttribute()) {
            final AttributeModel attModel = new AttributeModel();
            attModel.fromXml(xmlAtt);
            attModel.setFragment(fragment);
            attModels.add(attModel);
        }

        return attModels;
    }

    /**
     * Import model {@link ModelAttrAssoc} from input stream
     * @param pInputStream input stream
     * @return list of {@link ModelAttrAssoc}
     * @throws ImportException if error occurs!
     */
    public static List<ModelAttrAssoc> importModel(InputStream pInputStream,
            List<PluginConfiguration> plgConfigurations) throws ImportException {
        final Model xmlModel = read(pInputStream, Model.class);

        if (xmlModel.getAttribute().isEmpty() && xmlModel.getFragment().isEmpty()) {
            final String message = String
                    .format("Import for model %s is skipped because no attribute is bound!", xmlModel.getName());
            LOGGER.error(message);
            throw new ImportException(message);
        }

        final List<ModelAttrAssoc> modelAtts = new ArrayList<>();

        // Manage model
        final fr.cnes.regards.modules.dam.domain.models.Model model = new fr.cnes.regards.modules.dam.domain.models.Model();
        model.fromXml(xmlModel);

        // Manage attribute (default fragment)
        for (Attribute xmlAtt : xmlModel.getAttribute()) {
            final ModelAttrAssoc modelAtt = new ModelAttrAssoc();
            modelAtt.fromXml(xmlAtt);
            modelAtt.setModel(model);
            modelAtt.getAttribute()
                    .setFragment(fr.cnes.regards.modules.dam.domain.models.attributes.Fragment.buildDefault());
            // Manage computation
            Computation computation = xmlAtt.getComputation();
            // A computation plugin has been specified
            if (computation != null) {
                manageComputation(xmlAtt, modelAtt, computation, plgConfigurations);
            }
            modelAtts.add(modelAtt);
        }

        for (Fragment xmlFragment : xmlModel.getFragment()) {
            // Manage fragment
            // CHECKSTYLE:OFF
            fr.cnes.regards.modules.dam.domain.models.attributes.Fragment fragment = new fr.cnes.regards.modules.dam.domain.models.attributes.Fragment();
            // CHECKSTYLE:ON
            fragment.fromXml(xmlFragment);

            for (Attribute xmlAtt : xmlFragment.getAttribute()) {
                final ModelAttrAssoc modelAtt = new ModelAttrAssoc();
                modelAtt.fromXml(xmlAtt);
                modelAtt.setModel(model);
                modelAtt.getAttribute().setFragment(fragment);
                // Manage computation
                Computation computation = xmlAtt.getComputation();
                // A computation plugin has been specified
                if (computation != null) {
                    manageComputation(xmlAtt, modelAtt, computation, plgConfigurations);
                }
                modelAtts.add(modelAtt);
            }
        }

        return modelAtts;
    }

    /**
     * Create PluginConfiguration from xml elements and ModelAttrAssoc and set it to ModelAttrAssoc
     * @param xmlAtt Attribute XML element
     * @param modelAtt currently built ModelAssocAttr
     * @param xmlComputation Computation XML element associated to attribute
     * @throws ImportException in case computation description is not coherent with attribute
     */
    private static void manageComputation(Attribute xmlAtt, ModelAttrAssoc modelAtt, Computation xmlComputation,
            List<PluginConfiguration> plgConfigurations) throws ImportException {
        Class<?> pluginClass = null;
        // If compute plugin is of type paramPluginType, parameters should be added as PluginParameter
        ParamPluginType xmlParamPluginType = null;
        if (xmlComputation.getCount() != null) {
            pluginClass = CountPlugin.class;
        } else if (xmlComputation.getSumCompute() != null) {
            xmlParamPluginType = xmlComputation.getSumCompute();
            // Depends on attribute type
            switch (modelAtt.getAttribute().getType()) {
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
        } else if (xmlComputation.getMinCompute() != null) {
            xmlParamPluginType = xmlComputation.getMinCompute();
            // Depends on attribute type
            switch (modelAtt.getAttribute().getType()) {
                case DATE_ISO8601:
                    pluginClass = MinDateComputePlugin.class;
                    break;
                default:
                    String message = String.format("Only DATE attribute types are supported for min_compute plugin"
                                                           + " (attribute %s with type %s)",
                                                   xmlAtt.getName(),
                                                   xmlAtt.getType());
                    LOGGER.error(message);
                    throw new ImportException(message);
            }
        } else if (xmlComputation.getMaxCompute() != null) {
            xmlParamPluginType = xmlComputation.getMaxCompute();
            // Depends on attribute type
            switch (modelAtt.getAttribute().getType()) {
                case DATE_ISO8601:
                    pluginClass = MaxDateComputePlugin.class;
                    break;
                default:
                    String message = String.format("Only DATE attribute types are supported for max_compute plugin"
                                                           + " (attribute %s with type %s)",
                                                   xmlAtt.getName(),
                                                   xmlAtt.getType());
                    LOGGER.error(message);
                    throw new ImportException(message);
            }
        }
        createAndSetComputePluginConfiguration(xmlAtt, modelAtt, pluginClass, xmlParamPluginType, plgConfigurations);
    }

    private static void createAndSetComputePluginConfiguration(Attribute xmlAtt, ModelAttrAssoc modelAtt,
            Class<?> pluginClass, ParamPluginType xmlParamPluginType, List<PluginConfiguration> plgConfigurations)
            throws ImportException {
        if (pluginClass != null) {
            PluginMetaData plgMetaData = PluginUtils.createPluginMetaData(pluginClass);
            PluginConfiguration compConf = new PluginConfiguration(plgMetaData, xmlAtt.getComputation().getLabel());
            // Add plugin parameters (from attribute and associated fragment)
            Set<PluginParameter> parameters = Sets.newHashSet();
            // Some plugins need parameters (in this case, xmlParamPluginType contains them as attributes)
            if (xmlParamPluginType != null) {
                parameters.add(new PluginParameter("parameterAttributeName",
                                                   xmlParamPluginType.getParameterAttributeName()));
                // attribute fragment name being an optional parameter, lets check it
                if (xmlParamPluginType.getParameterAttributeFragmentName() != null) {
                    parameters.add(new PluginParameter("parameterAttributeFragmentName",
                                                       xmlParamPluginType.getParameterAttributeFragmentName()));
                }
            }
            compConf.setParameters(parameters);
            modelAtt.setComputationConf(compConf);
            // And create/update PluginConfiguration
            plgConfigurations.add(compConf);
        } else { // Unable to find PLuginClass
            String message = String.format("Unknown compute plugin for attribute %s", xmlAtt.getName());
            LOGGER.error(message);
            throw new ImportException(message);
        }
    }

    /**
     * Read {@link JAXBElement} from {@link InputStream}
     * @param <T> JAXB annotated class
     * @param pInputStream {@link InputStream}
     * @param pClass type of {@link JAXBElement} to read
     * @return {@link JAXBElement}
     * @throws ImportException if error occurs!
     */
    @SuppressWarnings("unchecked")
    private static <T> T read(InputStream pInputStream, Class<T> pClass) throws ImportException {

        try {
            // Init unmarshaller
            final JAXBContext jaxbContext = JAXBContext.newInstance(pClass);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // Enable validation
            final InputStream in = XmlExportHelper.class.getClassLoader()
                    .getResourceAsStream(XmlExportHelper.XML_SCHEMA_NAME);
            final StreamSource xsdSource = new StreamSource(in);
            jaxbUnmarshaller
                    .setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(xsdSource));

            // Unmarshall data
            return (T) jaxbUnmarshaller.unmarshal(pInputStream);

        } catch (JAXBException | SAXException e) {
            final String message = String.format("Error while importing data of %s type. %s", pClass, e.toString());
            LOGGER.error(message, e);
            throw new ImportException(message);
        }

    }
}
