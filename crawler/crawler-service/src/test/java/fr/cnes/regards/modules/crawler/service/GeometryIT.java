package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;

import fr.cnes.regards.framework.geojson.GeoJsonType;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.geojson.geometry.LineString;
import fr.cnes.regards.framework.geojson.geometry.MultiLineString;
import fr.cnes.regards.framework.geojson.geometry.MultiPoint;
import fr.cnes.regards.framework.geojson.geometry.MultiPolygon;
import fr.cnes.regards.framework.geojson.geometry.Point;
import fr.cnes.regards.framework.geojson.geometry.Polygon;
import fr.cnes.regards.framework.geojson.geometry.Unlocated;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.crawler.test.CrawlerConfiguration;
import fr.cnes.regards.modules.dam.dao.entities.IAbstractEntityRepository;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.service.entities.ICollectionService;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.dao.spatial.GeoHelper;
import fr.cnes.regards.modules.indexer.dao.spatial.ProjectGeoSettings;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.gson.MultitenantFlattenedAttributeAdapterFactoryEventHandler;
import fr.cnes.regards.modules.model.service.IModelService;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@ActiveProfiles("noschedule") // Disable scheduling, this will activate IngesterService during all tests
@TestPropertySource(locations = { "classpath:test.properties" })
public class GeometryIT {

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactoryEventHandler gsonAttributeFactoryHandler;

    @Autowired
    private ICollectionService collService;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity<?>> entityRepos;

    @Autowired
    private IEsRepository esRepos;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private ICrawlerAndIngesterService crawlerService;

    @Autowired
    private Gson gson;

    @Autowired
    private ProjectGeoSettings geoSettings;

    private Model collectionModel;

    private Collection collection;

    private Collection collection2;

    private static final String TENANT = "GEOM";

    @PostConstruct
    public void initEs() {

        // Simulate spring boot ApplicationStarted event to start mapping for each tenants.
        gsonAttributeFactoryHandler.onApplicationEvent(null);

        tenantResolver.forceTenant(TENANT);

        if (esRepos.indexExists(TENANT)) {
            esRepos.deleteIndex(TENANT);
        }
        esRepos.createIndex(TENANT);
        crawlerService.setConsumeOnlyMode(true);
    }

    @Before
    public void init() throws ModuleException {
        collectionModel = new Model();
        collectionModel.setName("model_1" + System.currentTimeMillis());
        collectionModel.setType(EntityType.COLLECTION);
        collectionModel.setVersion("1");
        collectionModel.setDescription("Test data object model");
        modelService.createModel(collectionModel);
    }

    @After
    public void clean() {
        // Don't use entity service to clean because events are published on RabbitMQ
        if (collection != null) {
            Utils.execute(entityRepos::deleteById, collection.getId());
        }
        if (collection2 != null) {
            Utils.execute(entityRepos::deleteById, collection2.getId());
        }
        if (collectionModel != null) {
            Utils.execute(modelService::deleteModel, collectionModel.getName());
        }
    }

    @Test
    public void testOnDbPoint() throws ModuleException, IOException {

        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL", "collection with geometry");
        collection.setNormalizedGeometry(IGeometry.point(IGeometry.position(41.12, -71.34)));
        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getNormalizedGeometry().getType() == GeoJsonType.POINT);
        Assert.assertTrue(collFromDB.getNormalizedGeometry() instanceof Point);

        Point point = (Point) collFromDB.getNormalizedGeometry();
        Assert.assertEquals(IGeometry.position(41.12, -71.34), point.getCoordinates());
    }

    @Test
    public void testOnEsPoint() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL", "collection with geometry");
        collection.setNormalizedGeometry(IGeometry.point(IGeometry.position(41.12, -71.34)));

        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getNormalizedGeometry().getType() == GeoJsonType.POINT);
        Point point = (Point) collFromEs.getNormalizedGeometry();
        Assert.assertEquals(IGeometry.position(41.12, -71.34), point.getCoordinates());
    }

    @Test
    public void testOnDbMultiPointLineString() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL1", "collection with geometry");
        MultiPoint multipoint = IGeometry.multiPoint(IGeometry.position(41.12, -71.34), IGeometry.position(42., -72.));
        collection.setNormalizedGeometry(multipoint);
        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getNormalizedGeometry().getType() == GeoJsonType.MULTIPOINT);
        Assert.assertTrue(collFromDB.getNormalizedGeometry() instanceof MultiPoint);
        multipoint = (MultiPoint) collFromDB.getNormalizedGeometry();

        double[][] ref1 = { { 41.12, -71.34 }, { 42., -72. } };
        Assert.assertArrayEquals(ref1, multipoint.getCoordinates().toArray());

        collection2 = new Collection(collectionModel, TENANT, "COL2", "another collection with geometry");
        LineString lineString = IGeometry.lineString(IGeometry
                .toLineStringCoordinates(IGeometry.position(41.12, -71.34), IGeometry.position(42., -72.)));
        collection2.setNormalizedGeometry(lineString);
        collService.create(collection2);

        final Collection coll2FromDB = collService.load(collection2.getId());
        Assert.assertTrue(coll2FromDB.getNormalizedGeometry().getType() == GeoJsonType.LINESTRING);
        Assert.assertTrue(coll2FromDB.getNormalizedGeometry() instanceof LineString);
        lineString = (LineString) coll2FromDB.getNormalizedGeometry();
        Assert.assertArrayEquals(ref1, lineString.getCoordinates().toArray());

    }

    @Test
    public void testOnEsMultiPointLineString() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL1", "collection with geometry");
        MultiPoint multipoint = IGeometry.multiPoint(IGeometry.position(41.12, -71.34), IGeometry.position(42., -72.));
        collection.setNormalizedGeometry(multipoint);

        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getNormalizedGeometry().getType() == GeoJsonType.MULTIPOINT);
        multipoint = collFromEs.getNormalizedGeometry();

        double[][] ref1 = { { 41.12, -71.34 }, { 42., -72. } };
        Assert.assertArrayEquals(ref1, multipoint.getCoordinates().toArray());

        collection2 = new Collection(collectionModel, TENANT, "COL2", "another collection with geometry");
        LineString lineString = IGeometry.lineString(IGeometry
                .toLineStringCoordinates(IGeometry.position(41.12, -71.34), IGeometry.position(42., -72.)));
        collection2.setNormalizedGeometry(lineString);

        esRepos.save(TENANT, collection2);
        esRepos.refresh(TENANT);

        final Collection coll2FromEs = esRepos.get(TENANT, collection2);
        Assert.assertTrue(coll2FromEs.getNormalizedGeometry().getType() == GeoJsonType.LINESTRING);
        lineString = (LineString) coll2FromEs.getNormalizedGeometry();
        Assert.assertArrayEquals(ref1, lineString.getCoordinates().toArray());

    }

    @Test
    public void testOnDbMultiLineStringPolygon() throws ModuleException, IOException {

        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL1", "collection with geometry");
        MultiLineString geometry = IGeometry
                .multiLineString(IGeometry.toLineStringCoordinates(IGeometry.position(41.12, -71.34),
                                                                   IGeometry.position(42., -72.)),
                                 IGeometry.toLineStringCoordinates(IGeometry.position(39.12, -70.34),
                                                                   IGeometry.position(38., -70.)));
        collection.setNormalizedGeometry(geometry);
        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getNormalizedGeometry().getType() == GeoJsonType.MULTILINESTRING);
        Assert.assertTrue(collFromDB.getNormalizedGeometry() instanceof MultiLineString);
        geometry = collFromDB.getNormalizedGeometry();

        double[][] ref1 = { { 41.12, -71.34 }, { 42., -72. } };
        Assert.assertArrayEquals(ref1, geometry.getCoordinates().get(0).toArray());
        double[][] ref2 = { { 39.12, -70.34 }, { 38., -70. } };
        Assert.assertArrayEquals(ref2, geometry.getCoordinates().get(1).toArray());
    }

    @Test
    public void testOnEsMultiLineStringPolygon() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL1", "collection with geometry");
        collection.setNormalizedGeometry(IGeometry
                .multiLineString(IGeometry.toLineStringCoordinates(IGeometry.position(41.12, -71.34),
                                                                   IGeometry.position(42., -72.)),
                                 IGeometry.toLineStringCoordinates(IGeometry.position(39.12, -70.34),
                                                                   IGeometry.position(38., -70.))));
        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getNormalizedGeometry().getType() == GeoJsonType.MULTILINESTRING);

        collection2 = new Collection(collectionModel, TENANT, "COL2", "another collection with geometry");
        // Polygon with hole defined using http://geojson.io
        Polygon polygon = IGeometry
                .polygon(IGeometry.toPolygonCoordinates(IGeometry
                        .toLinearRingCoordinates(IGeometry.position(1.4522552490234373, 43.62365009386727),
                                                 IGeometry.position(1.4556884765625, 43.57641143300888),
                                                 IGeometry.position(1.5250396728515625, 43.57641143300888),
                                                 IGeometry.position(1.531219482421875, 43.62215891380659),
                                                 IGeometry.position(1.4522552490234373, 43.62365009386727)),
                                                        IGeometry
                                                                .toLinearRingCoordinates(IGeometry
                                                                        .position(1.47216796875, 43.608736628843445),
                                                                                         IGeometry
                                                                                                 .position(1.4728546142578125,
                                                                                                           43.58735421230633),
                                                                                         IGeometry
                                                                                                 .position(1.50787353515625,
                                                                                                           43.58735421230633),
                                                                                         IGeometry
                                                                                                 .position(1.5085601806640625,
                                                                                                           43.60823944964323),
                                                                                         IGeometry
                                                                                                 .position(1.47216796875,
                                                                                                           43.608736628843445))));
        collection2.setNormalizedGeometry(polygon);

        esRepos.save(TENANT, collection2);
        esRepos.refresh(TENANT);

        final Collection coll2FromEs = esRepos.get(TENANT, collection2);
        Assert.assertTrue(coll2FromEs.getNormalizedGeometry().getType() == GeoJsonType.POLYGON);
        Assert.assertTrue(coll2FromEs.getNormalizedGeometry() instanceof Polygon);
    }

    @Test
    public void testOnDbMultiPolygon() throws ModuleException, IOException {

        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL1", "collection with geometry");
        MultiPolygon mp = IGeometry.multiPolygon(IGeometry.toPolygonCoordinates(IGeometry
                .toLinearRingCoordinates(IGeometry.position(102.0, 2.0), IGeometry.position(103.0, 2.0),
                                         IGeometry.position(103.0, 3.0), IGeometry.position(102.0, 3.0),
                                         IGeometry.position(102.0, 2.0))),
                                                 IGeometry.toPolygonCoordinates(IGeometry
                                                         .toLinearRingCoordinates(IGeometry.position(100.0, 0.0),
                                                                                  IGeometry.position(101.0, 0.0),
                                                                                  IGeometry.position(101.0, 1.0),
                                                                                  IGeometry.position(100.0, 1.0),
                                                                                  IGeometry.position(100.0, 0.0))));
        collection.setNormalizedGeometry(mp);
        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getNormalizedGeometry().getType() == GeoJsonType.MULTIPOLYGON);
        Assert.assertTrue(collFromDB.getNormalizedGeometry() instanceof MultiPolygon);
    }

    @Test
    public void testOnEsMultiPolygon() throws ModuleException, IOException {
        // Setting a geometry onto collection
        collection = new Collection(collectionModel, TENANT, "COL1", "collection with geometry");
        MultiPolygon mp = IGeometry.multiPolygon(IGeometry.toPolygonCoordinates(IGeometry
                .toLinearRingCoordinates(IGeometry.position(102.0, 2.0), IGeometry.position(103.0, 2.0),
                                         IGeometry.position(103.0, 3.0), IGeometry.position(102.0, 3.0),
                                         IGeometry.position(102.0, 2.0))),
                                                 IGeometry.toPolygonCoordinates(IGeometry
                                                         .toLinearRingCoordinates(IGeometry.position(100.0, 0.0),
                                                                                  IGeometry.position(101.0, 0.0),
                                                                                  IGeometry.position(101.0, 1.0),
                                                                                  IGeometry.position(100.0, 1.0),
                                                                                  IGeometry.position(100.0, 0.0))));
        collection.setNormalizedGeometry(mp);

        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getNormalizedGeometry().getType() == GeoJsonType.MULTIPOLYGON);
    }

    @Test
    public void testNoGeometry() throws ModuleException, IOException {

        collection = new Collection(collectionModel, TENANT, "COL1", "collection without geometry");

        collService.create(collection);

        final Collection collFromDB = collService.load(collection.getId());
        Assert.assertTrue(collFromDB.getNormalizedGeometry() instanceof Unlocated);

        // Index creation with geometry mapping
        esRepos.save(TENANT, collection);
        esRepos.refresh(TENANT);

        final Collection collFromEs = esRepos.get(TENANT, collection);
        Assert.assertTrue(collFromEs.getNormalizedGeometry() instanceof Unlocated);
    }

    @Test
    public void testDepartement() {
        IGeometry polygon = IGeometry
                .simpleClockwisePolygon(-0.10212095637964737, 47.064797190818005, -0.09805549141019346,
                                        47.0913509263054, -0.08590400471278307, 47.1010103974686, -0.04416541443323743,
                                        47.093242584398084, -0.035959245982801084, 47.12509157253751,
                                        -0.01073662557135027, 47.15750952036683, 0.01901485826766373, 47.17575785031973,
                                        0.03664297673522525, 47.160354293664795, 0.05382761471070587, 47.16373008265526,
                                        0.07637120084195233, 47.12393194888823, 0.13613090268194533, 47.121580554147506,
                                        0.1347219236888625, 47.1078700305567, 0.15685150997252176, 47.103344382080465,
                                        0.18146114771856547, 47.11438824881292, 0.20095302486598468, 47.09125887635545,
                                        0.1742206159576792, 47.07127519606643, 0.20800102904621295, 47.05323202361692,
                                        0.2455423301785185, 47.07128604001384, 0.2718544387898612, 47.04638904119169,
                                        0.29823142261819696, 47.05391872520871, 0.3093302509672645, 47.04413338267026,
                                        0.2986715021217093, 47.01959741824875, 0.3081773026194614, 46.99988348323911,
                                        0.30073625724988334, 46.97382765864781, 0.31122704760535835, 46.93783959199593,
                                        0.3248406111614126, 46.93065213256517, 0.3665152631758488, 46.94955720761298,
                                        0.4387096602536596, 46.929582071059045, 0.44480383249233546, 46.94114889283753,
                                        0.5027297280171042, 46.9579123909043, 0.5392917082788095, 46.960220880583776,
                                        0.5644222278144092, 46.95552996701939, 0.6015594112278456, 46.95910993478791,
                                        0.6011756847806528, 46.97309082433285, 0.5783449036180749, 46.97981081817415,
                                        0.5669489614197056, 47.00227037444166, 0.6188741963763473, 47.00746096968646,
                                        0.6362054885332586, 46.985451805972254, 0.692569282364634, 46.97430706301784,
                                        0.7062532556123257, 46.937154623849935, 0.704324392050786, 46.903293353155846,
                                        0.7521109761823577, 46.86086544125436, 0.8093213661875756, 46.827858093390724,
                                        0.8119017840639386, 46.79450796471714, 0.8279817587178017, 46.776816236170745,
                                        0.8674688807081561, 46.74821910504847, 0.9010383725080395, 46.73609229312116,
                                        0.9279553620387494, 46.69539122809664, 0.9084421594440936, 46.6826701222059,
                                        0.9065157405117292, 46.64774942034653, 0.8943018154002151, 46.62573665182289,
                                        0.9158653080538118, 46.59663148752451, 0.9408377357519268, 46.581410311122475,
                                        0.987237034592862, 46.56556286906366, 1.0147644249831798, 46.56776399882199,
                                        1.0206069888359919, 46.53709643301311, 1.08759324171297, 46.538172760099805,
                                        1.1082908975477561, 46.53150940181887, 1.1491434460646581, 46.50220256803704,
                                        1.1349676142912506, 46.495261976149834, 1.152977857618218, 46.47295586110232,
                                        1.1516045347766355, 46.449236175811976, 1.2109861172875847, 46.42936675668184,
                                        1.1772799568053671, 46.38395162215293, 1.1575433154988817, 46.38874160395639,
                                        1.1309116151248964, 46.36438532205801, 1.123709186916179, 46.349062839671014,
                                        1.0969021987058567, 46.36211145047139, 1.0501574891827015, 46.36278094700801,
                                        1.0269954429337311, 46.34303442800396, 1.0217534424141506, 46.310696552617046,
                                        0.9884486047314835, 46.280360998092185, 0.968413453240399, 46.28602244975969,
                                        0.9326647831388527, 46.2820281162358, 0.9004933562268206, 46.28684421517437,
                                        0.8853264387584442, 46.266088374204195, 0.8614456196703202, 46.26163361249219,
                                        0.8454876223484477, 46.24206422629179, 0.8480099863968311, 46.228752872389514,
                                        0.8101043739851272, 46.22788528658486, 0.7957366162231452, 46.211086485296406,
                                        0.815811931396682, 46.19671831003433, 0.8334530172887465, 46.16655399123534,
                                        0.8234337839605995, 46.128581728488264, 0.8095104050353692, 46.13820512038027,
                                        0.7801581669353024, 46.13213998977384, 0.7469045366399735, 46.13857472178201,
                                        0.710577167789481, 46.13088444115974, 0.684060820103776, 46.11934698105583,
                                        0.6874528960648788, 46.09725460634645, 0.6086469693380949, 46.0896841812107,
                                        0.6060084175300469, 46.077142007163026, 0.5749744983104493, 46.07899630556071,
                                        0.5646140785657598, 46.089594364695195, 0.5381620342712992, 46.095338949157004,
                                        0.49249803098282, 46.1359535013665, 0.44325878141139535, 46.10157393893125,
                                        0.444533894881436, 46.09115151319632, 0.47596150384778746, 46.08270140200688,
                                        0.46914195555876237, 46.06154823906217, 0.4450902027032336, 46.05080094878019,
                                        0.4132869508404128, 46.04908867462494, 0.39105268436192897, 46.066346526515616,
                                        0.2798061440667491, 46.060966262312725, 0.2522183041176724, 46.07997966029883,
                                        0.21971869883786393, 46.09431625215903, 0.1973535052161626, 46.09555004152969,
                                        0.19108384573574536, 46.112280833588024, 0.21492180826491483, 46.13884556432992,
                                        0.22035458351339557, 46.15799975349338, 0.1870636693943682, 46.14851197384494,
                                        0.1551113740904502, 46.157168244596924, 0.13793977146169878, 46.18084966362293,
                                        0.1076952133925229, 46.1861301320678, 0.11343152573440447, 46.21220922906378,
                                        0.14344160353379778, 46.230114587841406, 0.12883677213786587, 46.26723154899869,
                                        0.15955968591576844, 46.26656979483444, 0.17232027940044886, 46.27860128086839,
                                        0.15218685361085643, 46.30314888540674, 0.16956912209813432, 46.31018694492532,
                                        0.17523938179388834, 46.33181773874165, 0.13759784994026253, 46.349395182700974,
                                        0.09780469789926086, 46.33079981164575, 0.07821800902702593, 46.304933538922825,
                                        0.03250915440256327, 46.32782028737164, 0.029657077300027247, 46.349192346191,
                                        0.013846004253531347, 46.35706042377505, 0.03421997901294537, 46.37349679947992,
                                        0.02069664723879619, 46.38800352143432, 2.6572855997630953E-4,
                                        46.392018635848565, -0.017092824348942416, 46.4113223945137,
                                        -0.010457661947033809, 46.44881280383021, -0.01707071907246923,
                                        46.47527516260208, -0.04230359002464692, 46.470028260342126,
                                        -0.03135125528751674, 46.52498101941566, -0.007157529433821307,
                                        46.52327699327565, 0.007895566679664658, 46.547974321223606,
                                        -0.008022109474557896, 46.567543137522954, 0.020058959008376207,
                                        46.58512799711242, 0.022854639746489266, 46.614530685176334,
                                        0.0031057671136598976, 46.61079878720693, -0.026547974206768066,
                                        46.62886079422371, -0.0653244004897615, 46.63246151135352, -0.03338532562574275,
                                        46.65364917507512, -0.040426190194721524, 46.66371702380725,
                                        -0.006947998344661607, 46.68290982229758, 7.042720140891931E-4,
                                        46.71623331657764, 0.03826535834799053, 46.73158052408886, 0.005731121087621784,
                                        46.75454225312974, -0.014979708609133745, 46.75637013577674,
                                        -0.02232550667364259, 46.78961529950765, -0.020316336069380297,
                                        46.812653925006536, -0.04568987388716529, 46.83210506753766,
                                        -0.007773096486773423, 46.84747452695961, 0.01660746491547145,
                                        46.83496346394018, 0.02561539858266648, 46.85287480114874,
                                        -0.027263869318720418, 46.87992322868147, -0.009032410299882686,
                                        46.90747700605923, -0.022215929347250134, 46.938478291692846,
                                        -0.044186809693518486, 46.95837119466449, -0.038153345460296736,
                                        46.98939008519809, -0.06675773993609899, 46.99387700352016,
                                        -0.08113082448484565, 47.01267677238524);
        Collection coll = new Collection(collectionModel, TENANT, "TOTO", "collection");
        coll.setNormalizedGeometry(polygon);
        coll.setWgs84(GeoHelper.normalize(polygon));
        System.out.println(gson.toJson(coll.getWgs84(), IGeometry.class));
        esRepos.save(TENANT, coll);

        esRepos.refresh(TENANT);
    }

    @Test
    @Ignore("Some problem occurs with the pole")
    public void testConstellation() {
        Mockito.when(geoSettings.getShouldManagePolesOnGeometries()).thenReturn(true);
        Mockito.when(geoSettings.getCrs()).thenReturn(Crs.ASTRO);
        IGeometry polygon = IGeometry.simplePolygon(1.5334, -81.804, 1.5663, -74.304, -8.0022, -74.3125, -36.8152,
                                                    -74.4545, -85.8049, -74.9745, -83.134, -82.4583, -150.8889,
                                                    -83.1201, 111.6521, -82.7759, 109.0197, -85.2614, 48.2329, -84.5554,
                                                    50.0917, -82.0645);
        System.out.println(gson.toJson(polygon, IGeometry.class));
        Collection coll = new Collection(collectionModel, TENANT, "TITI", "Octans");
        coll.setNormalizedGeometry(GeoHelper.normalize(polygon));
        IGeometry wgs84Geometry = GeoHelper.transform(polygon, Crs.ASTRO, Crs.WGS_84);
        coll.setWgs84(GeoHelper.normalize(wgs84Geometry));
        System.out.println(gson.toJson(coll.getWgs84(), IGeometry.class));
        esRepos.save(TENANT, coll);

        esRepos.refresh(TENANT);
    }
}
