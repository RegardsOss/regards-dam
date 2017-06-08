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
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;

/**
 *
 * @DirtiesContext is mandatory, we have issue with context cleaning because of MockMvc
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@TestPropertySource("classpath:test.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class IAccessGroupClientIT extends AbstractRegardsWebIT {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(IAccessGroupClientIT.class);

    @Value("${server.address}")
    private String serverAddress;

    /**
     * Client to test
     */
    private IAccessGroupClient client;

    /**
     * Feign security manager
     */
    @Autowired
    private FeignSecurityManager feignSecurityManager;

    @Before
    public void init() {
        jwtService.injectMockToken(DEFAULT_TENANT, DEFAULT_ROLE);
        client = FeignClientBuilder.build(new TokenClientProvider<>(IAccessGroupClient.class,
                "http://" + serverAddress + ":" + getPort(), feignSecurityManager));
        FeignSecurityManager.asSystem();
    }

    /**
     *
     * Check that the access group Feign Client handle the pagination parameters.
     *
     * @since 1.0-SNAPSHOT
     */
    @Test
    public void testRetrieveAccessGroupsList() {
        final ResponseEntity<PagedResources<Resource<AccessGroup>>> accessGroups = client.retrieveAccessGroupsList(0,
                                                                                                                   10);
        Assert.assertTrue(accessGroups.getStatusCode().equals(HttpStatus.OK));
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
