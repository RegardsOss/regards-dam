/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 *
 * Repository tests
 *
 * @author msordi
 *
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { AttributeModelTestConfiguration.class })
@DirtiesContext
public class AttributeModelTest {

    /**
     * Mock tenant
     */
    private static final String TENANT = "PROJECT";

    /**
     * Mock role
     */
    private static final String ROLE = "ROLE_USER";

    /**
     * Attribute model repository
     */
    @Autowired
    private IAttributeModelRepository attModelRepository;

    /**
     * JWT service
     */
    @Autowired
    private JWTService jwtService;

    @Test
    public void getAllAttributes() {

        jwtService.injectMockToken(TENANT, ROLE);

        final Iterable<AttributeModel> atts = attModelRepository.findAll();
        Assert.assertNotNull(atts);
    }
}
