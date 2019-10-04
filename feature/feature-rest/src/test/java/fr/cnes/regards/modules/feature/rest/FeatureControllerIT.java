package fr.cnes.regards.modules.feature.rest;

import java.util.UUID;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.modules.feature.dto.Feature;

@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature",
		"regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
		"spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles(value = { "testAmqp" })
public class FeatureControllerIT extends AbstractRegardsTransactionalIT {

	@Test
	public void testPublishEndPoint() throws Exception {
		Feature featureToAdd;
		featureToAdd = new Feature();
		featureToAdd.setEntityType(EntityType.DATA);
		featureToAdd.setModel("model");
		featureToAdd.setGeometry(IGeometry.point(IGeometry.position(10.0, 20.0)));
		featureToAdd.setUrn(new UniformResourceName(OAISIdentifier.SIP, EntityType.DATA, "peps", UUID.randomUUID(), 1));
		RequestBuilderCustomizer requestBuilderCustomizer = customizer().expectStatusOk();

		performDefaultPost(FeatureController.PATH_FEATURE, featureToAdd, requestBuilderCustomizer,
				"Should retrieve request id").andDo(MockMvcResultHandlers.print());

	}

}
