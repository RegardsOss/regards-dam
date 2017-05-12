/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.schema.Attribute;
import fr.cnes.regards.modules.models.schema.Fragment;
import fr.cnes.regards.modules.models.schema.Model;
import fr.cnes.regards.modules.models.service.exception.ImportException;

/**
 * Help to manage model XML import based on XML schema definition
 *
 * @author Marc Sordi
 *
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
     *
     * @param pInputStream
     *            input stream
     * @return list of {@link AttributeModel} linked to same {@link Fragment}
     * @throws ImportException
     *             if error occurs!
     */
    public static List<AttributeModel> importFragment(InputStream pInputStream) throws ImportException {
        final Fragment xmlFragment = read(pInputStream, Fragment.class);

        if (xmlFragment.getAttribute().isEmpty()) {
            final String message = String.format("Import for fragment %s is skipped because no attribute is bound!",
                                                 xmlFragment.getName());
            LOGGER.error(message);
            throw new ImportException(message);
        }

        final List<AttributeModel> attModels = new ArrayList<>();

        // Manage fragment
        // CHECKSTYLE:OFF
        fr.cnes.regards.modules.models.domain.attributes.Fragment fragment = new fr.cnes.regards.modules.models.domain.attributes.Fragment();
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
     *
     * @param pInputStream
     *            input stream
     * @return list of {@link ModelAttrAssoc}
     * @throws ImportException
     *             if error occurs!
     */
    public static List<ModelAttrAssoc> importModel(InputStream pInputStream) throws ImportException {
        final Model xmlModel = read(pInputStream, Model.class);

        if (xmlModel.getAttribute().isEmpty() && xmlModel.getFragment().isEmpty()) {
            final String message = String.format("Import for model %s is skipped because no attribute is bound!",
                                                 xmlModel.getName());
            LOGGER.error(message);
            throw new ImportException(message);
        }

        final List<ModelAttrAssoc> modelAtts = new ArrayList<>();

        // Manage model
        final fr.cnes.regards.modules.models.domain.Model model = new fr.cnes.regards.modules.models.domain.Model();
        model.fromXml(xmlModel);

        // Manage attribute (default fragment)
        for (Attribute xmlAtt : xmlModel.getAttribute()) {
            final ModelAttrAssoc modelAtt = new ModelAttrAssoc();
            modelAtt.fromXml(xmlAtt);
            modelAtt.setModel(model);
            modelAtt.getAttribute()
                    .setFragment(fr.cnes.regards.modules.models.domain.attributes.Fragment.buildDefault());
            modelAtts.add(modelAtt);
        }

        for (Fragment xmlFragment : xmlModel.getFragment()) {
            // Manage fragment
            // CHECKSTYLE:OFF
            fr.cnes.regards.modules.models.domain.attributes.Fragment fragment = new fr.cnes.regards.modules.models.domain.attributes.Fragment();
            // CHECKSTYLE:ON
            fragment.fromXml(xmlFragment);

            for (Attribute xmlAtt : xmlFragment.getAttribute()) {
                final ModelAttrAssoc modelAtt = new ModelAttrAssoc();
                modelAtt.fromXml(xmlAtt);
                modelAtt.setModel(model);
                modelAtt.getAttribute().setFragment(fragment);
                modelAtts.add(modelAtt);
            }
        }

        return modelAtts;
    }

    /**
     * Read {@link JAXBElement} from {@link InputStream}
     *
     * @param <T>
     *            JAXB annotated class
     * @param pInputStream
     *            {@link InputStream}
     * @param pClass
     *            type of {@link JAXBElement} to read
     * @return {@link JAXBElement}
     * @throws ImportException
     *             if error occurs!
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
            final String message = String.format("Error while importing data of %s type", pClass);
            LOGGER.error(message, e);
            throw new ImportException(message);
        }

    }
}
