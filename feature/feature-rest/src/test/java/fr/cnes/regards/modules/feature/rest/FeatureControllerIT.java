package fr.cnes.regards.modules.feature.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.validation.MapBindingResult;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureCollection;
import fr.cnes.regards.modules.feature.dto.FeatureMetadata;
import fr.cnes.regards.modules.feature.dto.StorageMetadata;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.service.IFeatureValidationService;

@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature",
        "regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
        "spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles(value = { "testAmqp", "noscheduler" })
@ContextConfiguration(classes = { AbstractMultitenantServiceTest.ScanningConfiguration.class })
public class FeatureControllerIT extends AbstractRegardsTransactionalIT {

    @Autowired
    private IFeatureValidationService validationMock;

    /**
     *
     */
    private static final String FEATURE_CREATION_REQUEST_ERROR = "Something goes wrong during FeatureCreationRequest creation";

    @Test
    public void testCreateValidFeatureCreationRequest() throws Exception {
        Feature featureToAdd;
        featureToAdd = new Feature();
        featureToAdd.setEntityType(EntityType.DATA);
        featureToAdd.setModel("model");
        featureToAdd.setGeometry(IGeometry.point(IGeometry.position(10.0, 20.0)));
        featureToAdd.setUrn(null);
        featureToAdd.setId("id");
        FeatureCollection collection = new FeatureCollection();
        collection.add(featureToAdd);
        List<StorageMetadata> metadata = new ArrayList<StorageMetadata>();
        metadata.add(StorageMetadata.build("id"));
        Mockito.when(validationMock.validate(Mockito.any(), Mockito.any()))
                .thenReturn(new MapBindingResult(new HashMap<>(), Feature.class.getName()));
        collection.setMetadata(FeatureMetadata.build("me", "session", metadata));

        RequestBuilderCustomizer requestBuilderCustomizer = customizer().expectStatusCreated();
        performDefaultPost(FeatureController.PATH_FEATURES, collection, requestBuilderCustomizer,
                           FEATURE_CREATION_REQUEST_ERROR).andDo(MockMvcResultHandlers.print());

    }

    @Test
    public void testCreateUnValidFeatureCreationRequest() throws Exception {
        Feature featureToAdd;
        featureToAdd = new Feature();
        featureToAdd.setEntityType(EntityType.DATA);
        featureToAdd.setGeometry(IGeometry.point(IGeometry.position(10.0, 20.0)));
        featureToAdd.setUrn(null);
        featureToAdd.setModel("model");

        FeatureCollection collection = new FeatureCollection();
        collection.add(featureToAdd);
        List<StorageMetadata> metadata = new ArrayList<StorageMetadata>();
        metadata.add(StorageMetadata.build("id"));

        collection.setMetadata(FeatureMetadata.build("me", "session", metadata));
        MapBindingResult errors = new MapBindingResult(new HashMap<>(), Feature.class.getName());
        errors.reject("error code");
        Mockito.when(validationMock.validate(Mockito.any(), Mockito.any())).thenReturn(errors);
        RequestBuilderCustomizer requestBuilderCustomizer = customizer().expectStatusConflict();
        performDefaultPost(FeatureController.PATH_FEATURES, collection, requestBuilderCustomizer,
                           FEATURE_CREATION_REQUEST_ERROR).andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateValidFeatureUpdateRequest() throws Exception {
        Feature feature;
        feature = new Feature();
        feature.setEntityType(EntityType.DATA);
        feature.setModel("model");
        feature.setGeometry(IGeometry.point(IGeometry.position(10.0, 20.0)));
        feature.setUrn(null);
        feature.setId("id");
        feature.setUrn(FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, EntityType.DATA, "tenant",
                                                        UUID.randomUUID(), 1));
        FeatureCollection collection = new FeatureCollection();
        collection.add(feature);
        List<StorageMetadata> metadata = new ArrayList<StorageMetadata>();
        metadata.add(StorageMetadata.build("id"));

        collection.setMetadata(FeatureMetadata.build("me", "session", metadata));
        Mockito.when(validationMock.validate(Mockito.any(), Mockito.any()))
                .thenReturn(new MapBindingResult(new HashMap<>(), Feature.class.getName()));
        RequestBuilderCustomizer requestBuilderCustomizer = customizer().expectStatusCreated();
        performDefaultPatch(FeatureController.PATH_FEATURES, collection, requestBuilderCustomizer,
                            FEATURE_CREATION_REQUEST_ERROR).andDo(MockMvcResultHandlers.print());

    }
}
