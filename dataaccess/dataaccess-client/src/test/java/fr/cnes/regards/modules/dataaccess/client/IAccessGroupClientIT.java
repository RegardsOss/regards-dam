/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.client;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.feign.support.ResponseEntityDecoder;
import org.springframework.cloud.netflix.feign.support.SpringMvcContract;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.RequestMethod;

import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.hystrix.HystrixFeign;
import fr.cnes.regards.framework.feign.annotation.TokenClientProvider;
import fr.cnes.regards.framework.security.role.DefaultRole;
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
     *
     * Check that the access group Feign Client handle the pagination parameters.
     *
     * @since 1.0-SNAPSHOT
     */
    @Test
    public void testRetrieveAccessGroupsList() {
        try {
            authService.setAuthorities(DEFAULT_TENANT, IAccessGroupClient.PATH_ACCESS_GROUPS, RequestMethod.GET,
                                       DefaultRole.INSTANCE_ADMIN.toString());
            jwtService.injectToken(DEFAULT_TENANT, DefaultRole.INSTANCE_ADMIN.toString(), "");
            final IAccessGroupClient accessGroupClient = HystrixFeign.builder().contract(new SpringMvcContract())
                    .encoder(new GsonEncoder()).decoder(new ResponseEntityDecoder(new GsonDecoder()))
                    .target(new TokenClientProvider<>(IAccessGroupClient.class,
                            "http://" + serverAddress + ":" + getPort()));
            final ResponseEntity<PagedResources<Resource<AccessGroup>>> accessGroups = accessGroupClient
                    .retrieveAccessGroupsList(0, 10);
            Assert.assertTrue(accessGroups.getStatusCode().equals(HttpStatus.OK));
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
