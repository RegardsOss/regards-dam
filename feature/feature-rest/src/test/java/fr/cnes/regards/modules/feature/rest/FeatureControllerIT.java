package fr.cnes.regards.modules.feature.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.util.MimeType;
import org.springframework.validation.MapBindingResult;

import fr.cnes.regards.framework.geojson.GeoJsonMediaType;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.ConstrainedFields;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureFile;
import fr.cnes.regards.modules.feature.dto.FeatureFileAttributes;
import fr.cnes.regards.modules.feature.dto.FeatureFileLocation;
import fr.cnes.regards.modules.feature.dto.FeatureMetadata;
import fr.cnes.regards.modules.feature.dto.FeatureSessionMetadata;
import fr.cnes.regards.modules.feature.dto.FeatureUpdateCollection;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.StorageMetadata;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.service.IFeatureValidationService;
import fr.cnes.regards.modules.model.client.IModelAttrAssocClient;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.model.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.model.service.exception.ImportException;
import fr.cnes.regards.modules.model.service.xml.IComputationPluginService;
import fr.cnes.regards.modules.model.service.xml.XmlImportHelper;

@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature",
        "regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
        "spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles(value = { "testAmqp", "noscheduler" })
@ContextConfiguration(classes = { AbstractMultitenantServiceTest.ScanningConfiguration.class })
public class FeatureControllerIT extends AbstractRegardsTransactionalIT {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureControllerIT.class);

    @Autowired
    private IFeatureValidationService validationMock;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    private IComputationPluginService cps;

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory factory;

    @Autowired
    private IModelAttrAssocClient modelAttrAssocClientMock;

    private static final String FEATURE_CREATION_REQUEST_ERROR = "Something goes wrong during FeatureCreationRequest creation";

    private static final String FEATURE_UPDATE_REQUEST_ERROR = "Something goes wrong during FeatureUpdateRequest creation";

    /**
     * Mock model client importing model specified by its filename
     * @param filename model filename found using {@link Class#getResourceAsStream(String)}
     * @return mocked model name
     */
    public String mockModelClient(String filename, IComputationPluginService cps,
            MultitenantFlattenedAttributeAdapterFactory factory, String tenant,
            IModelAttrAssocClient modelAttrAssocClientMock) {

        try (InputStream input = FeatureControllerIT.class.getResourceAsStream(filename)) {
            // Import model
            Iterable<ModelAttrAssoc> assocs = XmlImportHelper.importModel(input, cps);

            // Translate to resources and attribute models and extract model name
            String modelName = null;
            List<AttributeModel> atts = new ArrayList<>();
            List<Resource<ModelAttrAssoc>> resources = new ArrayList<>();
            for (ModelAttrAssoc assoc : assocs) {
                atts.add(assoc.getAttribute());
                resources.add(new Resource<ModelAttrAssoc>(assoc));
                if (modelName == null) {
                    modelName = assoc.getModel().getName();
                }
            }

            // Property factory registration
            factory.registerAttributes(tenant, atts);

            // Mock client
            Mockito.when(modelAttrAssocClientMock.getModelAttrAssocs(modelName))
                    .thenReturn(ResponseEntity.ok(resources));

            return modelName;
        } catch (IOException | ImportException e) {
            String errorMessage = "Cannot import model";
            LOGGER.debug(errorMessage);
            throw new AssertionError(errorMessage);
        }
    }

    @Test
    public void testCreateValidFeatureCreationRequest() throws Exception {

        Feature featureToAdd = initValidFeature();
        FeatureUpdateCollection collection = new FeatureUpdateCollection();
        collection.add(featureToAdd);
        List<StorageMetadata> metadata = new ArrayList<StorageMetadata>();
        metadata.add(StorageMetadata.build("id"));
        // we will mock validation plugin and consider the feature is valid
        Mockito.when(validationMock.validate(Mockito.any(), Mockito.any()))
                .thenReturn(new MapBindingResult(new HashMap<>(), Feature.class.getName()));
        collection.setMetadata(FeatureSessionMetadata.build("me", "session", PriorityLevel.AVERAGE, metadata));

        RequestBuilderCustomizer requestBuilderCustomizer = customizer().expectStatusCreated();
        runtimeTenantResolver.forceTenant(this.getDefaultTenant());
        requestBuilderCustomizer.addHeader(HttpHeaders.CONTENT_TYPE, GeoJsonMediaType.APPLICATION_GEOJSON_UTF8_VALUE);

        documentFeatureCollectionRequestBody(requestBuilderCustomizer, false);

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

        FeatureUpdateCollection collection = new FeatureUpdateCollection();
        collection.add(featureToAdd);
        List<StorageMetadata> metadata = new ArrayList<StorageMetadata>();
        metadata.add(StorageMetadata.build("id"));

        collection.setMetadata(FeatureSessionMetadata.build("me", "session", PriorityLevel.AVERAGE, metadata));
        MapBindingResult errors = new MapBindingResult(new HashMap<>(), Feature.class.getName());
        errors.reject("error code");
        // we will mock validation plugin and consider the feature is unvalid
        Mockito.when(validationMock.validate(Mockito.any(), Mockito.any())).thenReturn(errors);
        RequestBuilderCustomizer requestBuilderCustomizer = customizer().expectStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        requestBuilderCustomizer.addHeader(HttpHeaders.CONTENT_TYPE, GeoJsonMediaType.APPLICATION_GEOJSON_UTF8_VALUE);

        performDefaultPost(FeatureController.PATH_FEATURES, collection, requestBuilderCustomizer,
                           FEATURE_CREATION_REQUEST_ERROR).andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testCreateValidFeatureUpdateRequest() throws Exception {
        Feature feature = initValidFeature();
        FeatureUpdateCollection collection = new FeatureUpdateCollection();
        collection.add(feature);
        feature.setUrn(FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, EntityType.DATA, "tenant",
                                                        UUID.randomUUID(), 1));
        List<StorageMetadata> metadata = new ArrayList<StorageMetadata>();
        metadata.add(StorageMetadata.build("id"));

        collection.setMetadata(FeatureMetadata.build(PriorityLevel.AVERAGE, metadata));
        // we will mock validation plugin and consider the feature is valid
        Mockito.when(validationMock.validate(Mockito.any(), Mockito.any()))
                .thenReturn(new MapBindingResult(new HashMap<>(), Feature.class.getName()));
        RequestBuilderCustomizer requestBuilderCustomizer = customizer().expectStatusCreated();
        documentFeatureCollectionRequestBody(requestBuilderCustomizer, true);
        requestBuilderCustomizer.addHeader(HttpHeaders.CONTENT_TYPE, GeoJsonMediaType.APPLICATION_GEOJSON_UTF8_VALUE);
        runtimeTenantResolver.forceTenant(this.getDefaultTenant());

        performDefaultPatch(FeatureController.PATH_FEATURES, collection, requestBuilderCustomizer,
                            FEATURE_UPDATE_REQUEST_ERROR).andDo(MockMvcResultHandlers.print());
    }

    private Feature initValidFeature() {
        String model = mockModelClient("feature_model_01.xml", cps, factory, this.getDefaultTenant(),
                                       modelAttrAssocClientMock);
        Feature feature;
        feature = new Feature();
        feature.setEntityType(EntityType.DATA);
        feature.setModel(model);
        feature.setGeometry(IGeometry.point(IGeometry.position(10.0, 20.0)));
        feature.setUrn(null);
        feature.setId("id");
        feature.addProperty(IProperty.buildString("data_type", "TYPE01"));
        feature.getFiles().add(FeatureFile.build(FeatureFileAttributes
                .build(DataType.DOCUMENT, MimeType.valueOf("application/xml"), "filename", 100l, "MD5", "checksum"),
                                                 FeatureFileLocation.build("www.test.com", "storage")));
        return feature;
    }

    @Test
    public void testCreateUnValidFeatureUpdateRequest() throws Exception {
        Feature feature;
        feature = new Feature();
        feature.setEntityType(EntityType.DATA);
        feature.setModel("model");
        feature.setGeometry(IGeometry.point(IGeometry.position(10.0, 20.0)));
        feature.setUrn(null);
        feature.setId("id");
        feature.setUrn(FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, EntityType.DATA, "tenant",
                                                        UUID.randomUUID(), 1));
        FeatureUpdateCollection collection = new FeatureUpdateCollection();
        collection.add(feature);
        List<StorageMetadata> metadata = new ArrayList<StorageMetadata>();
        metadata.add(StorageMetadata.build("id"));

        collection.setMetadata(FeatureMetadata.build(PriorityLevel.AVERAGE, metadata));
        // we will mock validation plugin and consider the feature is valid
        Mockito.when(validationMock.validate(Mockito.any(), Mockito.any()))
                .thenReturn(new MapBindingResult(new HashMap<>(), Feature.class.getName()));
        MapBindingResult errors = new MapBindingResult(new HashMap<>(), Feature.class.getName());
        errors.reject("error code");
        // we will mock validation plugin and consider the feature is unvalid
        Mockito.when(validationMock.validate(Mockito.any(), Mockito.any())).thenReturn(errors);

        RequestBuilderCustomizer requestBuilderCustomizer = customizer().expectStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        requestBuilderCustomizer.addHeader(HttpHeaders.CONTENT_TYPE, GeoJsonMediaType.APPLICATION_GEOJSON_UTF8_VALUE);

        performDefaultPatch(FeatureController.PATH_FEATURES, collection, requestBuilderCustomizer,
                            FEATURE_CREATION_REQUEST_ERROR).andDo(MockMvcResultHandlers.print());

    }

    private void documentFeatureCollectionRequestBody(RequestBuilderCustomizer requestBuilderCustomizer,
            boolean isUpdate) {
        ConstrainedFields fields = new ConstrainedFields(FeatureUpdateCollection.class);

        List<FieldDescriptor> lfd = new ArrayList<FieldDescriptor>();

        lfd.add(fields.withPath("metadata.storages", "Target storages"));
        lfd.add(fields.withPath("metadata.storages[].pluginBusinessId", "Storage identifier"));
        lfd.add(fields.withPath("metadata.storages[].targetTypes",
                                "List of data object types accepted by this storage location (when storing AIPs)"));
        lfd.add(fields.withPath("features[].entityType", "Entity Type"));
        // in case of update we will set the urn
        if (isUpdate) {
            lfd.add(fields.withPath("features[].urn",
                                    "Unique feature identifer based on provider identifier with versionning"));
        } else { // in case of creation we create session metadata
            lfd.add(fields.withPath("metadata.session", "The session name"));
            lfd.add(fields.withPath("metadata.sessionOwner", "The session owner"));
        }
        lfd.add(fields.withPath("features[].model", "Model"));
        lfd.add(fields.withPath("features[].id", "Technical id"));
        lfd.add(fields.withPath("features[].model", "Model"));
        lfd.add(fields.withPath("features[].geometry", "GeoJson Coordinates"));
        lfd.add(fields.withPath("features[].properties", "Properties"));
        lfd.add(fields.withPath("features[].files[].locations[].storage", "Storage"));
        lfd.add(fields.withPath("features[].files[].locations[].url", "Url location"));
        lfd.add(fields.withPath("features[].files[].attributes.dataType", "Data type"));
        lfd.add(fields.withPath("features[].files[].attributes.mimeType", "Media type"));
        lfd.add(fields.withPath("features[].files[].attributes.filename", "File name"));
        lfd.add(fields.withPath("features[].files[].attributes.filesize", "File size"));
        lfd.add(fields.withPath("features[].files[].attributes.algorithm", "Algorith for checksum computation"));
        lfd.add(fields.withPath("features[].files[].attributes.checksum", "Checksum"));

        requestBuilderCustomizer.document(PayloadDocumentation
                .relaxedRequestFields(Attributes.attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TITLE)
                        .value("Feature Collection manipulation")), lfd.toArray(new FieldDescriptor[lfd.size()])));
    }
}
