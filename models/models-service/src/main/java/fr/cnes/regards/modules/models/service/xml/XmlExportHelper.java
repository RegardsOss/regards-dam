/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

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
 *
 * @author Marc Sordi
 *
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

    public static void exportFragment(OutputStream pOutputStream,
            fr.cnes.regards.modules.models.domain.attributes.Fragment pFragment, List<AttributeModel> pAttributes)
            throws ExportException {
        write(pOutputStream, Fragment.class, toXmlFragment(pFragment, pAttributes));
    }

    public static void exportModel(OutputStream pOutputStream, fr.cnes.regards.modules.models.domain.Model pModel,
            List<ModelAttrAssoc> pAttributes) throws ExportException {
        write(pOutputStream, Model.class, toXmlModel(pModel, pAttributes));
    }

    /**
     * Build a {@link Model} based on a {@link fr.cnes.regards.modules.models.domain.Model} and its related
     * {@link ModelAttrAssoc}
     *
     * @param pModel
     *            {@link fr.cnes.regards.modules.models.domain.Model}
     * @param pAttributes
     *            list of {@link ModelAttrAssoc}
     * @return serializable {@link Model}
     * @throws ExportException
     *             if error occurs!
     */
    private static Model toXmlModel(fr.cnes.regards.modules.models.domain.Model pModel,
            List<ModelAttrAssoc> pAttributes) throws ExportException {

        // Manage model
        final Model model = pModel.toXml();

        // Manage attributes from both default fragment (i.e. default namespace) and from particular fragment
        final Map<String, Fragment> fragmentMap = new HashMap<>();

        if ((pAttributes != null) && !Iterables.isEmpty(pAttributes)) {
            for (ModelAttrAssoc modelAtt : pAttributes) {
                dispatchAttribute(fragmentMap, modelAtt);
            }
        }

        // Get default fragment
        final Fragment defaultFragment = fragmentMap
                .remove(fr.cnes.regards.modules.models.domain.attributes.Fragment.getDefaultName());
        if (defaultFragment != null) {
            model.getAttribute().addAll(defaultFragment.getAttribute());
        }

        // Manage attributes in particular fragments
        final List<Fragment> fragments = fragmentMap.entrySet().stream().map(x -> x.getValue())
                .collect(Collectors.toList());
        if (!fragments.isEmpty()) {
            model.getFragment().addAll(fragments);
        }

        return model;
    }

    /**
     * Dispatch {@link ModelAttrAssoc} in its related fragment navigating through {@link AttributeModel}
     *
     * @param pFragmentMap
     *            {@link Fragment} map
     * @param pModelAtt
     *            {@link ModelAttrAssoc} to dispatch
     */
    private static void dispatchAttribute(Map<String, Fragment> pFragmentMap, ModelAttrAssoc pModelAtt) {
        final AttributeModel attModel = pModelAtt.getAttribute();
        final fr.cnes.regards.modules.models.domain.attributes.Fragment fragment = attModel.getFragment();

        // Init or retrieve fragment DTO
        Fragment xmlFragment = pFragmentMap.get(fragment.getName());
        if (xmlFragment == null) {
            // Init fragment
            xmlFragment = new Fragment();
            xmlFragment.setName(fragment.getName());
            xmlFragment.setDescription(fragment.getDescription());
            pFragmentMap.put(fragment.getName(), xmlFragment);
        }
        xmlFragment.getAttribute().add(pModelAtt.toXml());
    }

    /**
     * Build a {@link Fragment} based on a {@link fr.cnes.regards.modules.models.domain.attributes.Fragment} and its
     * related {@link AttributeModel}
     *
     * @param pFragment
     *            {@link fr.cnes.regards.modules.models.domain.attributes.Fragment}
     * @param pAttributes
     *            list of {@link AttributeModel}
     * @return {@link Fragment}
     */
    private static Fragment toXmlFragment(fr.cnes.regards.modules.models.domain.attributes.Fragment pFragment,
            List<AttributeModel> pAttributes) {

        // Manage fragment
        final Fragment xmlFragment = pFragment.toXml();

        // Manage attributes
        if ((pAttributes != null) && !Iterables.isEmpty(pAttributes)) {
            for (AttributeModel att : pAttributes) {
                xmlFragment.getAttribute().add(att.toXml());
            }
        }

        return xmlFragment;
    }

    /**
     * Write {@link JAXBElement} to {@link OutputStream}
     *
     * @param <T>
     *            {@link JAXBElement} type
     * @param pOutputStream
     *            {@link OutputStream}
     * @param pClass
     *            {@link JAXBElement} type
     * @param pJaxbElement
     *            {@link JAXBElement}
     * @throws ExportException
     *             if error occurs!
     */
    private static <T> void write(OutputStream pOutputStream, Class<T> pClass, T pJaxbElement) throws ExportException {

        try {
            // Init marshaller
            final JAXBContext jaxbContext = JAXBContext.newInstance(pClass);
            final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // Format output
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Enable validation
            final InputStream in = XmlExportHelper.class.getClassLoader().getResourceAsStream(XML_SCHEMA_NAME);
            final StreamSource xsdSource = new StreamSource(in);
            jaxbMarshaller
                    .setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(xsdSource));

            // Marshall data
            jaxbMarshaller.marshal(pJaxbElement, pOutputStream);
        } catch (JAXBException | SAXException e) {
            final String message = String.format("Error while exporting data of %s type", pClass);
            LOGGER.error(message, e);
            throw new ExportException(message);
        }

    }
}
