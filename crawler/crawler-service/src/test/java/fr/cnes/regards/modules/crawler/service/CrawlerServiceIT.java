/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.framework.test.util.Beans;
import fr.cnes.regards.framework.utils.plugins.PluginUtilsRuntimeException;
import fr.cnes.regards.modules.crawler.test.CrawlerConfiguration;
import fr.cnes.regards.modules.dam.dao.entities.IAbstractEntityRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDeletedEntityRepository;
import fr.cnes.regards.modules.dam.domain.datasources.AbstractAttributeMapping;
import fr.cnes.regards.modules.dam.domain.datasources.StaticAttributeMapping;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.DeletedEntity;
import fr.cnes.regards.modules.dam.domain.entities.feature.EntityFeature;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.gson.entities.MultitenantFlattenedAttributeAdapterFactoryEventHandler;
import fr.cnes.regards.modules.dam.service.entities.ICollectionService;
import fr.cnes.regards.modules.dam.service.entities.IDatasetService;
import fr.cnes.regards.modules.dam.service.entities.IEntitiesService;
import fr.cnes.regards.modules.dam.service.models.IModelService;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;

/**
 * Crawler service integration tests
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@ActiveProfiles("noschedule") // Disable scheduling, this will activate IngesterService during all tests
@Ignore
public class CrawlerServiceIT {

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactoryEventHandler gsonAttributeFactoryHandler;

    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlerServiceIT.class);

    @Value("${regards.tenant}")
    private String tenant;

    private Model modelColl;

    private Model modelDataset;

    private Model dataModel;

    private Dataset dataset1;

    private Dataset dataset2;

    private Dataset dataset3;

    private Collection coll1;

    private Collection coll2;

    private Collection coll3;

    private PluginConfiguration pluginConf;

    private PluginConfiguration dataSourcePluginConf;

    @Autowired
    private ICrawlerService crawlerService;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IEntitiesService entitiesService;

    @Autowired
    private ICollectionService collService;

    @Autowired
    private IDatasetService dsService;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity<EntityFeature>> entityRepos;

    @Autowired
    private IEsRepository esRepos;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private IDeletedEntityRepository deletedEntityRepository;

    @SuppressWarnings("unused")
    private List<AbstractAttributeMapping> modelAttrMapping;

    @Before
    public void setUp() {

        // Simulate spring boot ApplicationStarted event to start mapping for each tenants.
        gsonAttributeFactoryHandler.onApplicationEvent(null);

        tenantResolver.forceTenant(tenant);

        crawlerService.setConsumeOnlyMode(false);
    }

    @After
    public void clean() {
        // Don't use entity service to clean because events are published on RabbitMQ
        Utils.execute(entityRepos::delete, dataset1.getId());
        Utils.execute(entityRepos::delete, dataset2.getId());
        Utils.execute(entityRepos::delete, dataset3.getId());
        Utils.execute(entityRepos::delete, coll1.getId());
        Utils.execute(entityRepos::delete, coll2.getId());
        Utils.execute(entityRepos::delete, coll3.getId());

        Utils.execute(modelService::deleteModel, modelColl.getName());
        Utils.execute(modelService::deleteModel, modelDataset.getName());
        Utils.execute(modelService::deleteModel, dataModel.getName());
        Utils.execute(pluginService::deletePluginConfiguration, dataSourcePluginConf.getId());
        Utils.execute(pluginService::deletePluginConfiguration, pluginConf.getId());

    }

    private PluginConfiguration getOracleDataSource(final PluginConfiguration pluginConf) {
        // final List<PluginParameter> parameters = PluginParametersFactory.build()
        // .addParameterPluginConfiguration(OracleDataSourceFromSingleTablePlugin.CONNECTION_PARAM, pluginConf)
        // .addParameter(OracleDataSourceFromSingleTablePlugin.TABLE_PARAM, TABLE_NAME_TEST)
        // .addParameter(OracleDataSourceFromSingleTablePlugin.MODEL_PARAM, dataSourceModelMapping)
        // .getParameters();
        //
        // return PluginUtils.getPluginConfiguration(parameters, OracleDataSourceFromSingleTablePlugin.class,
        // Arrays.asList(PLUGIN_CURRENT_PACKAGE));
        // TODO replace by a Postgres plugin
        return null;
    }

    private PluginConfiguration getOracleConnectionConfiguration() {
        // final List<PluginParameter> parameters = PluginParametersFactory.build()
        // .addParameter(DefaultOracleConnectionPlugin.USER_PARAM, "toto")
        // .addParameter(DefaultOracleConnectionPlugin.PASSWORD_PARAM, "toto")
        // .addParameter(DefaultOracleConnectionPlugin.DB_HOST_PARAM, "toto")
        // .addParameter(DefaultOracleConnectionPlugin.DB_PORT_PARAM, "toto")
        // .addParameter(DefaultOracleConnectionPlugin.DB_NAME_PARAM, "toto")
        // .addParameter(DefaultOracleConnectionPlugin.MAX_POOLSIZE_PARAM, "1")
        // .addParameter(DefaultOracleConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();
        //
        // return PluginUtils.getPluginConfiguration(parameters, DefaultOracleConnectionPlugin.class,
        // Arrays.asList(PLUGIN_CURRENT_PACKAGE));
        // TODO replace by a Postgres plugin
        return null;
    }

    public void buildData1() throws ModuleException {
        if (esRepos.indexExists(tenant)) {
            esRepos.deleteIndex(tenant);
        }
        esRepos.createIndex(tenant);

        modelColl = Model.build("modelColl", "model desc", EntityType.COLLECTION);
        modelColl = modelService.createModel(modelColl);

        modelDataset = Model.build("modelDataset", "model desc", EntityType.DATASET);
        modelDataset = modelService.createModel(modelDataset);

        dataModel = new Model();
        dataModel.setName("model_1_" + System.currentTimeMillis());
        dataModel.setType(EntityType.DATA);
        dataModel.setVersion("1");
        dataModel.setDescription("Test data object model");
        modelService.createModel(dataModel);
        modelAttrMapping = Collections.singletonList(new StaticAttributeMapping(AbstractAttributeMapping.PRIMARY_KEY,
                AttributeType.INTEGER, "DATA_OBJECTS_ID"));
        pluginConf = getOracleConnectionConfiguration();
        pluginService.savePluginConfiguration(pluginConf);

        // DataSource PluginConf
        dataSourcePluginConf = getOracleDataSource(pluginConf);
        pluginService.savePluginConfiguration(dataSourcePluginConf);

        dataset1 = new Dataset(modelDataset, tenant, "DS1", "labelDs1");
        dataset1.setLicence("licence");
        dataset1.setProviderId("ProviderId1");
        dataset1.setDataSource(dataSourcePluginConf);
        // DS1 -> (G1) (group 1)
        dataset1.setGroups(Sets.newHashSet("G1"));
        dataset2 = new Dataset(modelDataset, tenant, "DS2", "labelDs2");
        dataset2.setLicence("licence");
        dataset2.setProviderId("ProviderId2");
        dataset2.setDataSource(dataSourcePluginConf);
        // DS2 -> (G2)
        dataset2.setGroups(Sets.newHashSet("G2"));
        dataset3 = new Dataset(modelDataset, tenant, "DS3", "labelDs3");
        dataset3.setLicence("licence");
        dataset3.setProviderId("ProviderId3");
        dataset3.setDataSource(dataSourcePluginConf);
        // DS3 -> (G3)
        dataset3.setGroups(Sets.newHashSet("G3"));
        // No tags on Datasets, it doesn't matter

        coll1 = new Collection(modelColl, tenant, "COL1", "coll1");
        coll1.setProviderId("ProviderId4");
        // C1 -> (DS1, DS2)
        coll1.setTags(Sets.newHashSet(dataset1.getIpId().toString(), dataset2.getIpId().toString()));
        coll2 = new Collection(modelColl, tenant, "COL2", "coll2");
        coll2.setProviderId("ProviderId5");
        // C2 -> (C1, DS3)
        coll2.setTags(Sets.newHashSet(coll1.getIpId().toString(), dataset3.getIpId().toString()));
        coll3 = new Collection(modelColl, tenant, "COL3", "coll3");
        coll3.setProviderId("ProviderId6");
        // C3 -> (DS3)
        coll3.setTags(Sets.newHashSet(dataset3.getIpId().toString()));
    }

    @Requirement("REGARDS_DSL_DAM_CAT_210")
    @Purpose("Le système doit publier automatiquement l’ensemble des jeux de données.")
    @Test
    public void testCrawl() throws InterruptedException, ModuleException, IOException, PluginUtilsRuntimeException {
        buildData1();

        crawlerService.startWork();
        coll1 = collService.create(coll1);
        LOGGER.info("create coll1 (" + coll1.getIpId() + ")");
        coll2 = collService.create(coll2);
        LOGGER.info("create coll2 (" + coll2.getIpId() + ")");
        coll3 = collService.create(coll3);
        LOGGER.info("create coll3 (" + coll3.getIpId() + ")");

        dataset1 = dsService.create(dataset1);
        LOGGER.info("create dataset1 (" + dataset1.getIpId() + ")");
        dataset2 = dsService.create(dataset2);
        LOGGER.info("create dataset2 (" + dataset2.getIpId() + ")");
        dataset3 = dsService.create(dataset3);
        LOGGER.info("create dataset3 (" + dataset3.getIpId() + ")");

        crawlerService.waitForEndOfWork();
        // To be sure that the crawlerService daemon has time to do its job
        Thread.sleep(20_000);

        // Don't forget managing groups update others entities
        coll1 = (Collection) entitiesService.loadWithRelations(coll1.getIpId());
        coll2 = (Collection) entitiesService.loadWithRelations(coll2.getIpId());
        coll3 = (Collection) entitiesService.loadWithRelations(coll3.getIpId());
        dataset1 = (Dataset) entitiesService.loadWithRelations(dataset1.getIpId());
        dataset2 = (Dataset) entitiesService.loadWithRelations(dataset2.getIpId());
        dataset3 = (Dataset) entitiesService.loadWithRelations(dataset3.getIpId());

        esRepos.refresh(tenant);
        Collection coll1Bis = esRepos.get(tenant, coll1);
        Assert.assertNotNull(coll1Bis);
        Assert.assertTrue(Beans.areEqual(coll1, coll1Bis, "getModel"));
        final Collection coll2Bis = esRepos.get(tenant, coll2);
        Assert.assertNotNull(coll2Bis);
        Assert.assertTrue(Beans.areEqual(coll2, coll2Bis, "getModel"));
        final Collection coll3Bis = esRepos.get(tenant, coll3);
        Assert.assertNotNull(coll3Bis);
        Assert.assertTrue(Beans.areEqual(coll3, coll3Bis, "getModel"));

        Dataset ds1Bis = esRepos.get(tenant, dataset1);
        Assert.assertNotNull(ds1Bis);
        Assert.assertTrue(Beans.areEqual(dataset1, ds1Bis, "getModel"));
        final Dataset ds2Bis = esRepos.get(tenant, dataset2);
        Assert.assertNotNull(ds2Bis);
        Assert.assertTrue(Beans.areEqual(dataset2, ds2Bis, "getModel"));
        final Dataset ds3Bis = esRepos.get(tenant, dataset3);
        Assert.assertNotNull(ds3Bis);
        Assert.assertTrue(Beans.areEqual(dataset3, ds3Bis, "getModel"));

        crawlerService.startWork();
        final OffsetDateTime suppressDate = OffsetDateTime.now();
        collService.delete(coll1.getId());
        dsService.delete(dataset1.getId());

        // To be sure that the crawlerService daemon has time to do its job
        // Thread.sleep(5000);
        crawlerService.waitForEndOfWork();

        esRepos.refresh(tenant);
        coll1Bis = esRepos.get(tenant, coll1);
        Assert.assertNull(coll1Bis);
        ds1Bis = esRepos.get(tenant, dataset1);
        Assert.assertNull(ds1Bis);

        // Check DeletedEntity has been created into database
        final OffsetDateTime now = OffsetDateTime.now();
        final Optional<DeletedEntity> deletedEntityOpt = deletedEntityRepository.findOneByIpId(coll1.getIpId());
        Assert.assertTrue(deletedEntityOpt.isPresent());
        final DeletedEntity deletedEntity = deletedEntityOpt.get();
        Assert.assertTrue(coll1.getCreationDate().equals(deletedEntity.getCreationDate()));
        Assert.assertTrue(coll1.getLastUpdate().equals(deletedEntity.getLastUpdate()));
        Assert.assertTrue(deletedEntity.getDeletionDate().isAfter(suppressDate));
        Assert.assertTrue(deletedEntity.getDeletionDate().isBefore(now));
    }
}
