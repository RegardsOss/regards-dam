/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
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
import fr.cnes.regards.client.core.TokenClientProvider;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.framework.security.utils.jwt.exception.JwtException;
import fr.cnes.regards.framework.test.integration.AbstractRegardsWebIT;
import fr.cnes.regards.modules.dataaccess.client.IUserClient;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@TestPropertySource("classpath:test.properties")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class IUserClientIT extends AbstractRegardsWebIT {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(IUserClientIT.class);

    @Value("${server.address}")
    private String serverAddress;

    @LocalServerPort
    private int serverPort;

    @Value("${hystrix.command.default.execution.isolation.strategy}")
    private String strategy;

    private IUserClient userClient;

    @Before
    public void init() throws JwtException {
        authService.setAuthorities(DEFAULT_TENANT, IUserClient.BASE_PATH, RequestMethod.GET,
                                   DefaultRole.INSTANCE_ADMIN.toString());
        jwtService.injectToken(DEFAULT_TENANT, DefaultRole.INSTANCE_ADMIN.toString());
        userClient = HystrixFeign.builder().contract(new SpringMvcContract()).encoder(new GsonEncoder())
                .decoder(new ResponseEntityDecoder(new GsonDecoder()))
                .target(new TokenClientProvider<>(IUserClient.class, "http://" + serverAddress + ":" + serverPort));
    }

    /**
     *
     * Check that the user controller from dataaccess Feign Client handle the pagination parameters.
     *
     * @since 1.0-SNAPSHOT
     */
    @Test
    public void testRetrieveAccessGroupsOfUser() {
        try {
            final ResponseEntity<PagedResources<Resource<AccessGroup>>> accessGroupOfUser = userClient
                    .retrieveAccessGroupsOfUser("user1@user1.user1", 0, 10);
            Assert.assertTrue(accessGroupOfUser.getStatusCode().equals(HttpStatus.OK));
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
