/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.dao;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTransactionalTest;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AbstractAccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.GroupAccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.QualityFilter;
import fr.cnes.regards.modules.dataaccess.domain.accessright.QualityLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.UserAccessRight;
import fr.cnes.regards.modules.entities.dao.IDataSetRepository;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * @author Sylvain Vissiere-Guerinet
 */
@TestPropertySource("classpath:test.properties")
public class AccessRightRepositoryIT extends AbstractDaoTransactionalTest {

    @Autowired
    private IAccessRightRepository<AbstractAccessRight> repo;

    @Autowired
    private IGroupAccessRightRepository groupRepo;

    @Autowired
    private IUserAccessRightRepository userRepo;

    @Autowired
    private IModelRepository modelRepo;

    @Autowired
    private IDataSetRepository dsRepo;

    @Autowired
    private IAccessGroupRepository agRepo;

    private User user1;

    private final String user1Email = "user1@user1.user1";

    private AccessGroup ag1;

    private AccessGroup ag2;

    private final String ag1Name = "AG1";

    private final String ag2Name = "AG2";

    private QualityFilter qf;

    private final AccessLevel al = AccessLevel.FULL_ACCES;

    private DataSet ds1;

    private DataSet ds2;

    private final String ds1Name = "DS1";

    private final String ds2Name = "DS2";

    private final String dsDesc = "DESC";

    private UserAccessRight uar1;

    private UserAccessRight uar2;

    private GroupAccessRight gar1;

    private GroupAccessRight gar2;

    @Before
    public void init() {
        qf = new QualityFilter(10, 0, QualityLevel.ACCEPTED);
        Model model = Model.build("model1", "desc", EntityType.DATASET);
        model = modelRepo.save(model);
        ds1 = new DataSet(model, getUrn(), "ds1");
        ds1.setLabel("label");
        ds1 = dsRepo.save(ds1);
        ds2 = new DataSet(model, getUrn(), "ds2");
        ds2.setLabel("label");
        ds2 = dsRepo.save(ds2);

        user1 = new User(user1Email);
        uar1 = new UserAccessRight(qf, al, ds1, user1);
        uar1 = userRepo.save(uar1);
        uar2 = new UserAccessRight(qf, al, ds2, user1);
        uar2 = userRepo.save(uar2);

        ag1 = new AccessGroup(ag1Name);
        ag1 = agRepo.save(ag1);
        gar1 = new GroupAccessRight(qf, al, ds1, ag1);
        gar1 = groupRepo.save(gar1);
        ag2 = new AccessGroup(ag2Name);
        ag2 = agRepo.save(ag2);
        gar2 = new GroupAccessRight(qf, al, ds2, ag2);
        gar2 = groupRepo.save(gar2);
    }

    private UniformResourceName getUrn() {
        return new UniformResourceName(OAISIdentifier.AIP, EntityType.DATASET, "PROJECT", UUID.randomUUID(), 1);
    }

    @Test
    public void testfindAllByAccessGroupName() {
        Page<GroupAccessRight> response = groupRepo.findAllByAccessGroup(ag1, new PageRequest(0, 10));
        Assert.assertTrue(response.getContent().contains(gar1));
        Assert.assertFalse(response.getContent().contains(gar2));
        Assert.assertFalse(response.getContent().contains(uar1));
        Assert.assertFalse(response.getContent().contains(uar2));
    }

    @Test
    public void testfindAllByDataSet() {
        Page<AbstractAccessRight> response = repo.findAllByDataSet(ds1, new PageRequest(0, 10));
        Assert.assertTrue(response.getContent().contains(gar1));
        Assert.assertTrue(response.getContent().contains(uar1));
        Assert.assertFalse(response.getContent().contains(gar2));
        Assert.assertFalse(response.getContent().contains(uar2));
    }

    @Test
    public void testfindAllByAccessGroupNameByDataSet() {
        Page<GroupAccessRight> response = groupRepo.findAllByAccessGroupAndDataSet(ag1, ds1, new PageRequest(0, 10));
        Assert.assertTrue(response.getContent().contains(gar1));
        Assert.assertFalse(response.getContent().contains(gar2));
        Assert.assertFalse(response.getContent().contains(uar1));
        Assert.assertFalse(response.getContent().contains(uar2));

        response = groupRepo.findAllByAccessGroupAndDataSet(ag1, ds2, new PageRequest(0, 10));
        Assert.assertFalse(response.hasContent());
    }

}
