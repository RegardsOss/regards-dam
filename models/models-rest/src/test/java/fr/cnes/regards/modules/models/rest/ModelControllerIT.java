/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author msordi
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ModelControllerIT {

    // private static final Logger LOG = LoggerFactory.getLogger(ModelControllerIT.class);
    //
    // private String jwt_;
    //
    // @Autowired
    // private MockMvc mvc_;
    //
    // @Autowired
    // private JWTService jwtService_;
    //
    // @Autowired
    // private IMethodAuthorizationService authService_;
    //
    // @Before
    // public void setup() {
    // String role = "USER";
    // String tenant = "PROJECT";
    // jwt_ = jwtService_.generateToken(tenant, "email", "MSI", role);
    // authService_.setAuthorities(tenant, "/models/attributes", RequestMethod.GET, role);
    // authService_.setAuthorities(tenant, "/models/attributes/{pAttributeId}", RequestMethod.GET, role, "ADMIN");
    // }
    //
    // /**
    // * @see test javadoc
    // * @requirement REGARDS_DSL_DAM_MOD_010
    // * @purpose Get model attributes to manage data models
    // */
    // @Requirement("REGARDS_DSL_DAM_MOD_010")
    // @Requirement("REGARDS_DSL_DAM_MOD_020")
    // @Purpose("Get model attributes to manage data models")
    // @Test
    // public void testGetAttributes() {
    //
    // try {
    // this.mvc_.perform(get("/models/attributes").header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt_))
    // .andExpect(status().isOk());
    // } catch (final Exception e) {
    // final String message = "Cannot reach model attributes";
    // LOG.error(message, e);
    // Assert.fail(message);
    // }
    // }

}
