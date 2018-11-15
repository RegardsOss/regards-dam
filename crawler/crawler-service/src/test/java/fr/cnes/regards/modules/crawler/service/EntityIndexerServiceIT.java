package fr.cnes.regards.modules.crawler.service;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.utils.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.encryption.exception.EncryptionException;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityOperationForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsIT;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.crawler.plugins.TestDataAccessRightPlugin;
import fr.cnes.regards.modules.crawler.plugins.TestDataSourcePlugin;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessLevel;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessRight;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.DataAccessLevel;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.DataAccessRight;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.QualityFilter;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.QualityLevel;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.service.dataaccess.IAccessGroupService;
import fr.cnes.regards.modules.dam.service.dataaccess.IAccessRightService;
import fr.cnes.regards.modules.dam.service.datasources.IDataSourceService;
import fr.cnes.regards.modules.dam.service.entities.IDatasetService;
import fr.cnes.regards.modules.dam.service.models.IModelService;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.service.ISearchService;
import fr.cnes.regards.modules.indexer.service.Searches;

@ActiveProfiles("indexer-service")
@TestPropertySource(locations = { "classpath:test-indexer.properties" }, properties = { "regards.tenant=entity_indexer",
        "spring.jpa.properties.hibernate.default_schema=entity_indexer" })
public class EntityIndexerServiceIT extends AbstractRegardsIT {

    private static String TENANT = "entity_indexer";

    @Autowired
    private IEsRepository esRepository;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IDatasetService datasetService;

    @Autowired
    private IDataSourceService datasourceService;

    @Autowired
    private IEntityIndexerService indexerService;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    private IAccessGroupService groupService;

    @Autowired
    private IAccessRightService rightsService;

    @Autowired
    private ISearchService searchService;

    private Dataset dataset;

    private PluginConfiguration datasource;

    private PluginConfiguration dataAccessPlugin;

    private Model model;

    private AccessGroup group1;

    private AccessGroup group2;

    private AccessGroup group3;

    private final List<DataObject> objects = Lists.newArrayList();

    @Autowired
    private IPluginService pluginService;

    private void initIndex(String index) {
        if (esRepository.indexExists(index)) {
            esRepository.deleteIndex(index);
        }
        esRepository.createIndex(index);
        String[] types = Arrays.stream(EntityType.values()).map(EntityType::toString)
                .toArray(length -> new String[length]);
        esRepository.setGeometryMapping(index, types);
    }

    @After
    public void clear() {
        try {
            rightsService.retrieveAccessRight("group1", dataset.getIpId()).ifPresent(ar -> {
                try {
                    rightsService.deleteAccessRight(ar.getId());
                } catch (ModuleException e) {
                    e.printStackTrace();
                }
            });
            rightsService.retrieveAccessRight("group2", dataset.getIpId()).ifPresent(ar -> {
                try {
                    rightsService.deleteAccessRight(ar.getId());
                } catch (ModuleException e) {
                    e.printStackTrace();
                }
            });
            rightsService.retrieveAccessRight("group3", dataset.getIpId()).ifPresent(ar -> {
                try {
                    rightsService.deleteAccessRight(ar.getId());
                } catch (ModuleException e) {
                    e.printStackTrace();
                }
            });
        } catch (ModuleException e) {
            // Nothing to do
            e.printStackTrace();
        }

        try {
            groupService.deleteAccessGroup("group1");
            groupService.deleteAccessGroup("group2");
            groupService.deleteAccessGroup("group3");
        } catch (EntityOperationForbiddenException | EntityNotFoundException e) {
            // Nothing to do
            e.printStackTrace();
        }

        if (dataset != null) {
            try {
                datasetService.delete(dataset.getId());
            } catch (ModuleException e) {
                // Nothing to do
            }
        }

        if (datasource != null) {
            try {
                pluginService.deletePluginConfiguration(datasource.getId());
            } catch (ModuleException e) {
                //Nothing to do
            }
        }

        if (dataAccessPlugin != null) {
            try {
                pluginService.deletePluginConfiguration(dataAccessPlugin.getId());
            } catch (ModuleException e) {
                //Nothing to do
            }
        }

        try {
            modelService.deleteModel("DS_MODEL");
            modelService.deleteModel("DO_MODEL");
        } catch (ModuleException e) {
            // Nothing to do
        }

    }

    @Before
    public void init() throws ModuleException {
        runtimeTenantResolver.forceTenant(TENANT);
        initIndex(TENANT);
        dataset = createDataset();
        group1 = createGroup("group1");
        group2 = createGroup("group2");
        group3 = createGroup("group3");
        objects.add(createObject("DO1", "DataObject 1"));
        objects.add(createObject("DO2", "DataObject 2"));
        objects.add(createObject("DO3", "DataObject 3"));
        objects.add(createObject("DO4", "DataObject 4"));
        objects.add(createObject("DO5", "DataObject 5"));
        objects.add(createObject("DO6", "DataObject 6"));
        dataAccessPlugin = createDataAccessPlugin();
        indexerService.createDataObjects(TENANT, datasource.getId().toString(), OffsetDateTime.now(), objects);
        indexerService.updateEntityIntoEs(TENANT, dataset.getIpId(), OffsetDateTime.now(), false);
    }

    private AccessGroup createGroup(String name) throws EntityAlreadyExistsException {
        return groupService.createAccessGroup(new AccessGroup(name));
    }

    private Dataset createDataset() throws ModuleException {
        model = createModel(EntityType.DATA, "DO_MODEL");
        dataset = new Dataset(createModel(EntityType.DATASET, "DS_MODEL"), TENANT, "dataset1", "Dataset 1");
        dataset.setSubsettingClause(ICriterion.all());
        datasource = createDataSource();
        dataset.setDataSource(datasource);
        dataset.setOpenSearchSubsettingClause("");
        // Create dataset
        return datasetService.create(dataset);
    }

    private Model createModel(EntityType type, String label) throws ModuleException {
        Model model = new Model();
        model.setName(label);
        model.setType(type);
        model.setVersion("1.0");
        model.setDescription("TEST");
        return modelService.createModel(model);
    }

    private PluginConfiguration createDataSource() throws ModuleException {
        Set<PluginParameter> param = PluginParametersFactory.build().addParameter(TestDataSourcePlugin.MODEL, model)
                .getParameters();
        return datasourceService
                .createDataSource(PluginUtils.getPluginConfiguration(param, TestDataSourcePlugin.class));
    }

    private PluginConfiguration createDataAccessPlugin()
            throws EntityInvalidException, EntityNotFoundException, EncryptionException {
        Set<PluginParameter> param = PluginParametersFactory.build()
                .addParameter(TestDataAccessRightPlugin.LABEL_PARAM, objects.get(0).getLabel()).getParameters();
        return pluginService
                .savePluginConfiguration(PluginUtils.getPluginConfiguration(param, TestDataAccessRightPlugin.class));
    }

    private DataObject createObject(String id, String label) {
        return new DataObject(model, TENANT, id, label);
    }

    @Test
    public void test() throws ModuleException {
        runtimeTenantResolver.forceTenant(TENANT);
        final SimpleSearchKey<AbstractEntity> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setSearchIndex(TENANT);
        Page<AbstractEntity> results = searchService.search(searchKey, 100, ICriterion.contains("groups", "group1"));
        Assert.assertEquals(0, results.getTotalElements());
        // Create an accessGroup
        AccessRight ar = new AccessRight(new QualityFilter(0, 0, QualityLevel.ACCEPTED), AccessLevel.FULL_ACCESS,
                dataset, group1);
        ar.setDataAccessRight(new DataAccessRight(DataAccessLevel.INHERITED_ACCESS));
        ar = rightsService.createAccessRight(ar);
        // All data should be only in group1
        indexerService.updateEntityIntoEs(TENANT, dataset.getIpId(), OffsetDateTime.now(), false);
        results = searchService.search(searchKey, 100, ICriterion.contains("groups", "group1"));
        Assert.assertEquals(objects.size(), results.getTotalElements());

        AccessRight ar2 = new AccessRight(new QualityFilter(0, 0, QualityLevel.ACCEPTED), AccessLevel.FULL_ACCESS,
                dataset, group2);
        ar2.setDataAccessRight(new DataAccessRight(DataAccessLevel.INHERITED_ACCESS));
        rightsService.createAccessRight(ar2);
        indexerService.updateEntityIntoEs(TENANT, dataset.getIpId(), OffsetDateTime.now(), false);
        // All data should be only in group1 and group2
        results = searchService.search(searchKey, 100, ICriterion.and(ICriterion.contains("groups", "group1"),
                                                                      ICriterion.contains("groups", "group2")));
        Assert.assertEquals(objects.size(), results.getTotalElements());
        rightsService.deleteAccessRight(ar.getId());
        indexerService.updateEntityIntoEs(TENANT, dataset.getIpId(), OffsetDateTime.now(), false);
        // All data should be only in group2
        results = searchService.search(searchKey, 100, ICriterion
                .and(ICriterion.not(ICriterion.contains("groups", "group1")), ICriterion.contains("groups", "group2")));
        Assert.assertEquals(objects.size(), results.getTotalElements());
        AccessRight ar3 = new AccessRight(new QualityFilter(0, 0, QualityLevel.ACCEPTED), AccessLevel.FULL_ACCESS,
                dataset, group3);
        DataAccessRight dataright = new DataAccessRight(DataAccessLevel.CUSTOM_ACCESS);
        ar3.setDataAccessPlugin(dataAccessPlugin);
        ar3.setDataAccessRight(dataright);
        rightsService.createAccessRight(ar3);
        indexerService.updateEntityIntoEs(TENANT, dataset.getIpId(), OffsetDateTime.now(), false);
        // All data should be in group2 and only one (DO1) in group3
        results = searchService.search(searchKey, 100,
                                       ICriterion.and(ICriterion.not(ICriterion.contains("groups", "group1")),
                                                      ICriterion.contains("groups", "group2"),
                                                      ICriterion.contains("groups", "group3")));
        Assert.assertEquals(1, results.getTotalElements());
        // Update plugin to change label to unknown label
        dataAccessPlugin.getParameter(TestDataAccessRightPlugin.LABEL_PARAM).setValue("unknown");
        pluginService.updatePluginConfiguration(dataAccessPlugin);
        indexerService.updateEntityIntoEs(TENANT, dataset.getIpId(), OffsetDateTime.now(), false);
        // All data should be in group2 and only one (DO1) in group3
        results = searchService.search(searchKey, 100,
                                       ICriterion.and(ICriterion.not(ICriterion.contains("groups", "group1")),
                                                      ICriterion.contains("groups", "group2"),
                                                      ICriterion.not(ICriterion.contains("groups", "group3"))));
        Assert.assertEquals(objects.size(), results.getTotalElements());

        // TODO delete plugin

        // TODO update accessright to no_access

        // TODO create another dataset with same objects

    }

}
