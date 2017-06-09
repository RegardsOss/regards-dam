/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.feign.FeignClientBuilder;
import fr.cnes.regards.framework.feign.TokenClientProvider;
import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.test.integration.AbstractRegardsWebIT;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@TestPropertySource("classpath:test.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class IAccessRightClientIT extends AbstractRegardsWebIT {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(IAccessRightClientIT.class);

    @Value("${server.address}")
    private String serverAddress;

    /**
     * Client to test
     */
    private IAccessRightClient client;

    /**
     * Feign security manager
     */
    @Autowired
    private FeignSecurityManager feignSecurityManager;

    @Before
    public void init() {
        jwtService.injectMockToken(DEFAULT_TENANT, DEFAULT_ROLE);
        client = FeignClientBuilder.build(new TokenClientProvider<>(IAccessRightClient.class,
                "http://" + serverAddress + ":" + getPort(), feignSecurityManager));
        FeignSecurityManager.asSystem();
    }

    /**
     *
     * Check that the access right Feign Client handle the pagination parameters.
     *
     * @since 1.0-SNAPSHOT
     */
    @Test
    public void testRetrieveAccessRightsList() {
        ResponseEntity<PagedResources<Resource<AccessRight>>> accessRights = client
                .retrieveAccessRightsList(null, null, null, 0, 10);
        Assert.assertTrue(accessRights.getStatusCode().equals(HttpStatus.OK));
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
