/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import com.google.gson.Gson;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTransactionalTest;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.indexer.domain.criterion.BooleanMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.exception.ImportException;
import fr.cnes.regards.modules.models.service.xml.XmlImportHelper;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@TestPropertySource("classpath:application-test.properties")
public class DatasetRepositoryIT extends AbstractDaoTransactionalTest {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetRepositoryIT.class);

    @Autowired
    private IModelRepository modelRepo;

    @Autowired
    private IDatasetRepository datasetRepo;

    private Dataset dataset;

    private Dataset dsDescription;

    private final String description = "some content";

    private Model srcModel;

    private AttributeModel attString;

    private AttributeModel attBoolean;

    private AttributeModel GEO_CRS;

    private AttributeModel Contact_Phone;

    @Autowired
    private Gson gson;

    @Before
    public void init() {
        Model pModel = Model.build("datasetModel", "pDescription", EntityType.DATASET);
        pModel = modelRepo.save(pModel);
        dataset = new Dataset(pModel, "pTenant", "dataset");
        dataset.setLicence("licence");
        dataset.setCreationDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));

        List<Long> confs = new ArrayList<>(2);
        confs.add(1L);
        confs.add(2L);

        setModelInPlace(importModel("sample-model-minimal.xml"));
        dataset.setSubsettingClause(getValidClause());
        dataset.setDataModel(srcModel.getId());

        String stringCLause = gson.toJson(dataset.getSubsettingClause());

        dataset = datasetRepo.save(dataset);

        dsDescription = new Dataset(srcModel, "pTenant", "dataSetWithDescription");
        dsDescription.setLicence("licence");
        dsDescription.setCreationDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
        dsDescription.setDescriptionFile(new DescriptionFile(description.getBytes(Charset.forName("utf-8")),
                MediaType.TEXT_MARKDOWN));
        dsDescription = datasetRepo.save(dsDescription);
    }

    @Test
    public void testFindOneDescription() {
        LOG.info("START OF find one DescriptionFile");
        Dataset result = datasetRepo.findOneDescriptionFile(dsDescription.getId());
        LOG.info("END OF find one DescriptionFile");
        Assert.assertNotNull(result.getDescriptionFile());
        Assert.assertArrayEquals(description.getBytes(Charset.forName("utf-8")),
                                 result.getDescriptionFile().getContent());
    }

    /**
     * @param pImportModel
     */
    private void setModelInPlace(List<ModelAttrAssoc> pImportModel) {
        srcModel = pImportModel.get(0).getModel();
        srcModel = modelRepo.save(srcModel);
        ModelAttrAssoc attStringModelAtt = pImportModel.stream()
                .filter(ma -> ma.getAttribute().getFragment().getName().equals(Fragment.getDefaultName())
                        && ma.getAttribute().getName().equals("att_string"))
                .findAny().get();
        attString = attStringModelAtt.getAttribute();
        ModelAttrAssoc attBooleanModelAtt = pImportModel.stream()
                .filter(ma -> ma.getAttribute().getFragment().getName().equals(Fragment.getDefaultName())
                        && ma.getAttribute().getName().equals("att_boolean"))
                .findAny().get();
        attBoolean = attBooleanModelAtt.getAttribute();
        ModelAttrAssoc CRS_CRSModelAtt = pImportModel.stream()
                .filter(ma -> ma.getAttribute().getFragment().getName().equals("GEO")
                        && ma.getAttribute().getName().equals("CRS"))
                .findAny().get();
        GEO_CRS = CRS_CRSModelAtt.getAttribute();

        ModelAttrAssoc Contact_PhoneModelAtt = pImportModel.stream()
                .filter(ma -> ma.getAttribute().getFragment().getName().equals("Contact")
                        && ma.getAttribute().getName().equals("Phone"))
                .findAny().get();
        Contact_Phone = Contact_PhoneModelAtt.getAttribute();
    }

    /**
     * @return
     */
    private ICriterion getValidClause() {
        // textAtt contains "testContains"
        ICriterion containsCrit = ICriterion.contains("attributes." + attString.getName(), "testContains");
        // textAtt ends with "testEndsWith"
        ICriterion endsWithCrit = ICriterion
                .endsWith("attributes." + GEO_CRS.getFragment().getName() + "." + GEO_CRS.getName(), "testEndsWith");
        // textAtt strictly equals "testEquals"
        ICriterion equalsCrit = ICriterion
                .eq("attributes." + Contact_Phone.getFragment().getName() + "." + Contact_Phone.getName(),
                        "testEquals");

        ICriterion booleanCrit = new BooleanMatchCriterion("attributes." + attBoolean.getName(), true);

        // All theses criterions (AND)
        ICriterion rootCrit = ICriterion.and(containsCrit, endsWithCrit, equalsCrit, booleanCrit);
        return rootCrit;
    }

    private List<ModelAttrAssoc> importModel(String pFilename) {
        InputStream input;
        try {
            input = Files.newInputStream(Paths.get("src", "test", "resources", pFilename));

            return XmlImportHelper.importModel(input);
        } catch (IOException | ImportException e) {
            LOG.debug("import of model failed", e);
            Assert.fail();
        }
        // never reached because test will fail first
        return null;
    }
}
