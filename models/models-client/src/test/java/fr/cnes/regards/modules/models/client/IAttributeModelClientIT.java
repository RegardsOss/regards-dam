/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.client;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.feign.FeignClientBuilder;
import fr.cnes.regards.framework.feign.TokenClientProvider;
import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsWebIT;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * Integration tests for {@link IAttributeModelClient}.
 *
 * @author Xavier-Alexandre Brochard
 */
@TestPropertySource("classpath:test.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class IAttributeModelClientIT extends AbstractRegardsWebIT {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(IAttributeModelClientIT.class);

    @Value("${server.address}")
    private String serverAddress;

    /**
     * Client to test
     */
    private IAttributeModelClient client;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * Feign security manager
     */
    @Autowired
    private FeignSecurityManager feignSecurityManager;

    @Before
    public void init() {
        client = FeignClientBuilder.build(new TokenClientProvider<>(IAttributeModelClient.class,
                "http://" + serverAddress + ":" + getPort(), feignSecurityManager));
        runtimeTenantResolver.forceTenant(DEFAULT_TENANT);
        FeignSecurityManager.asSystem();
    }

    /**
     * Check that the attribute model Feign Client can return the list of attribute models.
     */
    @Test
    public void getAttributesTest() {
        ResponseEntity<List<Resource<AttributeModel>>> attributeModels = client.getAttributes(null, null);
        Assert.assertTrue(attributeModels.getStatusCode().equals(HttpStatus.OK));
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
