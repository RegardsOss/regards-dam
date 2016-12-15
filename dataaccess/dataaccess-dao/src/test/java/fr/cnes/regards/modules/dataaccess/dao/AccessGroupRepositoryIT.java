/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.dao;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTransactionalTest;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@TestPropertySource("classpath:test.properties")
public class AccessGroupRepositoryIT extends AbstractDaoTransactionalTest {

    private static final String AG1_NAME = "AG1";

    private static final String AG2_NAME = "AG2";

    private static final User USER1 = new User("user1@user1.user1");

    @Autowired
    private IAccessGroupRepository dao;

    private AccessGroup ag1;

    private AccessGroup ag2;

    @Before
    public void init() {
        ag1 = new AccessGroup(AG1_NAME);
        ag1.addUser(USER1);
        ag1 = dao.save(ag1);
        ag2 = new AccessGroup(AG2_NAME);
        ag2.addUser(USER1);
        ag2 = dao.save(ag2);
    }

    @Test
    public void testFindOneByName() {
        AccessGroup agOfNameAG1 = dao.findOneByName(AG1_NAME);
        Assert.assertEquals(ag1, agOfNameAG1);
        AccessGroup agOfNameAG2 = dao.findOneByName(AG2_NAME);
        Assert.assertEquals(ag2, agOfNameAG2);
    }

    @Test
    public void testFindAllByUsers() {
        Set<AccessGroup> accessGroupsOfUser = dao.findAllByUsers(USER1);
        Assert.assertTrue(accessGroupsOfUser.contains(ag1));
        Assert.assertTrue(accessGroupsOfUser.contains(ag2));
    }

}
