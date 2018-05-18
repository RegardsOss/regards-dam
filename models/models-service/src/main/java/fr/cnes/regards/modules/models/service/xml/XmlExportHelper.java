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
package fr.cnes.regards.modules.models.service.xml;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.collect.Iterables;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.schema.Fragment;
import fr.cnes.regards.modules.models.schema.Model;
import fr.cnes.regards.modules.models.service.exception.ExportException;

/**
 * Help to manage model XML export based on XML schema definition
 * @author Marc Sordi
 */
public final class XmlExportHelper {

    /**
     * Related schema
     */
    // FIXME externalize model name
    public static final String XML_SCHEMA_NAME = "model_V1.0.xsd";

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlExportHelper.class);

    private XmlExportHelper() {
    }

    public static void exportFragment(OutputStream os,
            fr.cnes.regards.modules.models.domain.attributes.Fragment fragment, List<AttributeModel> attributes)
            throws ExportException {
        write(os, Fragment.class, toXmlFragment(fragment, attributes));
    }

    public static void exportModel(OutputStream os, fr.cnes.regards.modules.models.domain.Model model,
            List<ModelAttrAssoc> attributes) throws ExportException {
        write(os, Model.class, toXmlModel(model, attributes));
    }

    /**
     * Build a {@link Model} based on a {@link fr.cnes.regards.modules.models.domain.Model} and its related
     * {@link ModelAttrAssoc}
     * @param model {@link fr.cnes.regards.modules.models.domain.Model}
     * @param attributes list of {@link ModelAttrAssoc}
     * @return serializable {@link Model}
     * @throws ExportException if error occurs!
     */
    private static Model toXmlModel(fr.cnes.regards.modules.models.domain.Model model, List<ModelAttrAssoc> attributes)
            throws ExportException {

        // Manage model
        final Model xmlModel = model.toXml();

        // Manage attributes from both default fragment (i.e. default namespace) and from particular fragment
        final Map<String, Fragment> xmlFragmentMap = new HashMap<>();

        if ((attributes != null) && !Iterables.isEmpty(attributes)) {
            for (ModelAttrAssoc modelAtt : attributes) {
                dispatchAttribute(xmlFragmentMap, modelAtt);
            }
        }

        // Get default fragment
        final Fragment xmlDefaultFragment = xmlFragmentMap
                .remove(fr.cnes.regards.modules.models.domain.attributes.Fragment.getDefaultName());
        if (xmlDefaultFragment != null) {
            xmlModel.getAttribute().addAll(xmlDefaultFragment.getAttribute());
        }

        // Manage attributes in particular fragments
        if (!xmlFragmentMap.isEmpty()) {
            xmlModel.getFragment().addAll(xmlFragmentMap.values());
        }

        return xmlModel;
    }

    /**
     * Dispatch {@link ModelAttrAssoc} in its related fragment navigating through {@link AttributeModel}
     * @param fragmentMap {@link Fragment} map
     * @param modelAttrAssoc {@link ModelAttrAssoc} to dispatch
     */
    private static void dispatchAttribute(Map<String, Fragment> fragmentMap, ModelAttrAssoc modelAttrAssoc) {
        final AttributeModel attModel = modelAttrAssoc.getAttribute();
        final fr.cnes.regards.modules.models.domain.attributes.Fragment fragment = attModel.getFragment();

        // Init or retrieve fragment DTO
        Fragment xmlFragment = fragmentMap.get(fragment.getName());
        if (xmlFragment == null) {
            // Init fragment
            xmlFragment = new Fragment();
            xmlFragment.setName(fragment.getName());
            xmlFragment.setDescription(fragment.getDescription());
            fragmentMap.put(fragment.getName(), xmlFragment);
        }
        xmlFragment.getAttribute().add(modelAttrAssoc.toXml());
    }

    /**
     * Build a {@link Fragment} based on a {@link fr.cnes.regards.modules.models.domain.attributes.Fragment} and its
     * related {@link AttributeModel}
     * @param fragment {@link fr.cnes.regards.modules.models.domain.attributes.Fragment}
     * @param attributes list of {@link AttributeModel}
     * @return {@link Fragment}
     */
    private static Fragment toXmlFragment(fr.cnes.regards.modules.models.domain.attributes.Fragment fragment,
            List<AttributeModel> attributes) {

        // Manage fragment
        final Fragment xmlFragment = fragment.toXml();

        // Manage attributes
        if ((attributes != null) && !Iterables.isEmpty(attributes)) {
            for (AttributeModel att : attributes) {
                xmlFragment.getAttribute().add(att.toXml());
            }
        }

        return xmlFragment;
    }

    /**
     * Write {@link JAXBElement} to {@link OutputStream}
     * @param <T> {@link JAXBElement} type
     * @param os {@link OutputStream}
     * @param clazz {@link JAXBElement} type
     * @param jaxbElement {@link JAXBElement}
     * @throws ExportException if error occurs!
     */
    private static <T> void write(OutputStream os, Class<T> clazz, T jaxbElement) throws ExportException {

        try {
            // Init marshaller
            final JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // Format output
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Enable validation
            final InputStream in = XmlExportHelper.class.getClassLoader().getResourceAsStream(XML_SCHEMA_NAME);
            final StreamSource xsdSource = new StreamSource(in);
            jaxbMarshaller
                    .setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(xsdSource));

            // Marshall data
            jaxbMarshaller.marshal(jaxbElement, os);
        } catch (JAXBException | SAXException e) {
            final String message = String.format("Error while exporting data of %s type. %s", clazz, e.toString());
            LOGGER.error(message, e);
            throw new ExportException(message);
        }

    }
}
