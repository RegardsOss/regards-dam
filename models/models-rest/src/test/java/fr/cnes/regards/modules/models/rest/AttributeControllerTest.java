/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;

import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.RestrictionService;

/**
 *
 * Attribute controller test
 *
 * @author msordi
 *
 */
public class AttributeControllerTest {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(AttributeControllerTest.class);

    /**
     * Attribute service
     */
    private IAttributeModelService attributeServiceMocked;

    /**
     * Resource service
     */
    private IResourceService resourceServiceMocked;

    /**
     * {@link AttributeModelController}
     */
    private AttributeModelController attributeController;

    @Before
    public void init() {
        // Service
        attributeServiceMocked = Mockito.mock(IAttributeModelService.class);
        // Hateoas authorization
        resourceServiceMocked = Mockito.mock(IResourceService.class);
        final RestrictionService restrictionService = Mockito.mock(RestrictionService.class);
        // Init controller
        attributeController = new AttributeModelController(attributeServiceMocked, resourceServiceMocked,
                restrictionService);
    }

    @Test
    public void getAttributeTest() {
        final List<AttributeModel> attributes = new ArrayList<>();
        attributes.add(AttributeModelBuilder.build("NAME", AttributeType.STRING, "ForTests").withId(1L).get());
        attributes.add(AttributeModelBuilder.build("START_DATE", AttributeType.DATE_ISO8601, "ForTests").withId(2L).get());
        // CHECKSTYLE:OFF
        attributes.add(AttributeModelBuilder.build("STOP_DATE", AttributeType.DATE_ISO8601, "ForTests").withId(3L).get());
        // CHECKSTYLE:ON
        Mockito.when(attributeServiceMocked.getAttributes(null, null)).thenReturn(attributes);
        final ResponseEntity<List<Resource<AttributeModel>>> response = attributeController.getAttributes(null, null);
        Assert.assertEquals(attributes.size(), response.getBody().size());
    }

}
