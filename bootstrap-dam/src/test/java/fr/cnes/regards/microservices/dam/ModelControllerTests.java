/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.dam;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import fr.cnes.regards.microservices.core.security.jwt.JWTService;

/**
 * @author msordi
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class ModelControllerTests {

    private static final Logger LOG = LoggerFactory.getLogger(ModelControllerTests.class);

    static String jwt_;

    @Autowired
    private MockMvc mvc_;

    @BeforeClass
    public static void staticSetup() {
        JWTService jwtService_ = new JWTService();
        jwtService_.setSecret("123456789");
        jwt_ = jwtService_.generateToken("PROJECT", "email", "MSI", "USER");
    }

    @Test
    public void testGetAttributes() {

        try {
            this.mvc_.perform(get("/models/attributes").header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt_))
                    .andExpect(status().isOk());
        }
        catch (Exception e) {
            String message = "Cannot reach model attributes";
            LOG.error(message, e);
            Assert.fail(message);
        }
    }

}
