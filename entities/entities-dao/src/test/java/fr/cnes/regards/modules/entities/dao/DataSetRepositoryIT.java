/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTransactionalTest;
import fr.cnes.regards.modules.datasources.dao.IDataSourceRepository;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@TestPropertySource("classpath:application-test.properties")
public class DataSetRepositoryIT extends AbstractDaoTransactionalTest {

    @Autowired
    private IModelRepository modelRepo;

    @Autowired
    private IDataSourceRepository dataSourceRepo;

    @Autowired
    private IDataSetRepository dataSetRepo;

    private DataSet dataset;

    @Before
    public void init() {
        Model pModel = Model.build("datasetModel", "pDescription", EntityType.DATASET);
        pModel = modelRepo.save(pModel);
        dataset = new DataSet(pModel,
                new UniformResourceName(OAISIdentifier.AIP, EntityType.DATASET, "pTenant", UUID.randomUUID(), 1),
                "dataset");

        List<Long> confs = new ArrayList<>(2);
        confs.add(1L);
        confs.add(2L);
        dataset.setPluginConfigurationIds(confs);

        Model srcModel = Model.build("dataModel", "pDescription", EntityType.DATA);
        srcModel = modelRepo.save(srcModel);
        DataSource dataSource = new DataSource(srcModel);
        dataSource = dataSourceRepo.save(dataSource);
        dataset.setDataSource(dataSource);

        dataset = dataSetRepo.save(dataset);
    }

    @Test
    public void testFindOneWithPluginConfigurations() {
        DataSet result = dataSetRepo.findOneWithPluginConfigurations(dataset.getId());
        Assert.assertTrue(result.getPluginConfigurationIds() != null);
        Assert.assertTrue(result.getPluginConfigurationIds().size() == 2);
        Assert.assertTrue(result.getPluginConfigurationIds().contains(1L));
        Assert.assertTrue(result.getPluginConfigurationIds().contains(2L));
    }

}
