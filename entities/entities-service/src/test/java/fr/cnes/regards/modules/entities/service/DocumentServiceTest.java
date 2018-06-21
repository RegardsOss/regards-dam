package fr.cnes.regards.modules.entities.service;

import java.io.IOException;
import java.time.OffsetDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.entities.dao.IDocumentLSRepository;
import fr.cnes.regards.modules.entities.dao.IDocumentRepository;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;

@TestPropertySource(locations = { "classpath:test.properties" })
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { ServiceConfiguration.class })
@MultitenantTransactional
public class DocumentServiceTest {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceTest.class);

    @Autowired
    private IDocumentService documentService;

    @Autowired
    private IDocumentLSRepository documentLSRepository;

    @Autowired
    private IDocumentRepository documentRepository;

    @Autowired
    private IModelRepository modelRepository;

    private Model model1;

    private Document document1;

    @Before
    public void init() throws ModuleException {
        model1 = Model.build("modelName2", "model desc", EntityType.DOCUMENT);
        model1 = modelRepository.save(model1);

        document1 = new Document(model1, "PROJECT", "document1");
        document1.setSipId("SipId2");
        document1.setLabel("label");
        document1.setCreationDate(OffsetDateTime.now());
        document1 = documentRepository.save(document1);

    }

    @Test
    @Commit
    public void testAddFiles() throws ModuleException, IOException {
        String fileLsUriTemplate = "/documents/" + document1.getId() + "/files/"
                + DocumentLSService.FILE_CHECKSUM_URL_TEMPLATE;
        MockMultipartFile mockMultipartFile = new MockMultipartFile("document1.xml", "document1.xml", "doc/xml",
                "content of my file".getBytes());
        MockMultipartFile mockMultipartFile2 = new MockMultipartFile("document2.png", "document2.png", "image/png",
                "some pixels informations".getBytes());
        MultipartFile[] multipartFiles = { mockMultipartFile, mockMultipartFile2 };
        Document updatedDoc = documentService.addFiles(document1.getId(), multipartFiles, fileLsUriTemplate);

        Assert.assertEquals(updatedDoc.getDocumentFiles().size(), 2);
        Assert.assertEquals(documentLSRepository.count(), 2);
        documentService.deleteDocumentAndFiles(document1.getId());
        Assert.assertEquals(documentLSRepository.count(), 0);
    }

}
