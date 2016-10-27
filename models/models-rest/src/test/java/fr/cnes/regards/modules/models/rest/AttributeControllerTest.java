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

import fr.cnes.regards.framework.hateoas.DefaultResourceService;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.security.endpoint.MethodAuthorizationService;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelFactory;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeService;

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
    private static final Logger LOG = LoggerFactory.getLogger(AttributeControllerTest.class);

    /**
     * Attribute service
     */
    private IAttributeService attributeServiceMocked;

    /**
     * Resource service
     */
    private IResourceService resourceServiceMocked;

    /**
     * {@link AttributeController}
     */
    private AttributeController attributeController;

    @Before
    public void init() {
        // Service
        attributeServiceMocked = Mockito.mock(IAttributeService.class);
        // Hateoas authorization
        final MethodAuthorizationService authService = Mockito.mock(MethodAuthorizationService.class);
        resourceServiceMocked = new DefaultResourceService(authService);
        // Init controller
        attributeController = new AttributeController(attributeServiceMocked, resourceServiceMocked);
    }

    @Test
    public void getAttributeTest() {
        final List<AttributeModel> attributes = new ArrayList<>();
        attributes.add(AttributeModelFactory.build(1L, "NAME", AttributeType.STRING));
        attributes.add(AttributeModelFactory.build(2L, "START_DATE", AttributeType.DATE_ISO8601));
        attributes.add(AttributeModelFactory.build(3L, "STOP_DATE", AttributeType.DATE_ISO8601));
        Mockito.when(attributeServiceMocked.getAttributes(null)).thenReturn(attributes);

        final ResponseEntity<List<Resource<AttributeModel>>> response = attributeController.getAttributes(null);

        Assert.assertEquals(attributes.size(), response.getBody().size());
    }

}
