/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.rest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.modules.accessrights.client.IProjectUsersClient;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.dataaccess.dao.IAccessGroupRepository;
import fr.cnes.regards.modules.dataaccess.dao.IGroupAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.dao.IUserAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.DataAccessLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.DataAccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.GroupAccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.QualityFilter;
import fr.cnes.regards.modules.dataaccess.domain.accessright.QualityLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.UserAccessRight;
import fr.cnes.regards.modules.dataaccess.service.AccessGroupService;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@MultitenantTransactional
@TestPropertySource("classpath:test.properties")
public class AccessRightControllerIT extends AbstractRegardsTransactionalIT {

    private static final Logger LOG = LoggerFactory.getLogger(AccessRightControllerIT.class);

    private static final String ACCESS_RIGHTS_ERROR_MSG = "Should have been an answer";

    @Autowired
    private IGroupAccessRightRepository groupRepo;

    @Autowired
    private IUserAccessRightRepository userRepo;

    @Autowired
    private IModelRepository modelRepo;

    @Autowired
    private IDatasetRepository dsRepo;

    @Autowired
    private IAccessGroupRepository agRepo;

    private User user1;

    private final String user1Email = "user1@user1.user1";

    private AccessGroup ag1;

    private AccessGroup ag2;

    private final String ag1Name = "AG1";

    private final String ag2Name = "AG2";

    private QualityFilter qf;

    private final AccessLevel al = AccessLevel.FULL_ACCESS;

    private DataAccessRight dar;

    private Dataset ds1;

    private Dataset ds2;

    private final String ds1Name = "DS1";

    private final String ds2Name = "DS2";

    private final String dsDesc = "DESC";

    private UserAccessRight uar1;

    private UserAccessRight uar2;

    private GroupAccessRight gar1;

    private GroupAccessRight gar2;

    private GroupAccessRight gar3;

    @Autowired
    private AccessGroupService agService;

    @Before
    public void init() {
        IProjectUsersClient projectUserClientMock = Mockito.mock(IProjectUsersClient.class);
        // Replace stubs by mocks
        ReflectionTestUtils.setField(agService, "projectUserClient", projectUserClientMock, IProjectUsersClient.class);
        Mockito.when(projectUserClientMock.retrieveProjectUser(Matchers.any()))
                .thenReturn(new ResponseEntity<>(new Resource<>(new ProjectUser()), HttpStatus.OK));

        qf = new QualityFilter(10, 0, QualityLevel.ACCEPTED);
        dar = new DataAccessRight(DataAccessLevel.NO_ACCESS);

        Model model = Model.build("model1", "desc", EntityType.DATASET);
        model = modelRepo.save(model);
        ds1 = new Dataset(model, "PROJECT", ds1Name);
        ds1.setLicence("licence");
        ds1.setDescriptionFile(new DescriptionFile(dsDesc));
        ds1 = dsRepo.save(ds1);
        ds2 = new Dataset(model, "PROJECT", ds2Name);
        ds2.setLicence("licence");
        ds2 = dsRepo.save(ds2);

        user1 = new User(user1Email);
        uar1 = new UserAccessRight(qf, al, ds1, user1);
        uar1 = userRepo.save(uar1);
        uar2 = new UserAccessRight(qf, al, ds2, user1);
        uar2 = userRepo.save(uar2);

        ag1 = new AccessGroup(ag1Name);
        ag1 = agRepo.save(ag1);
        gar1 = new GroupAccessRight(qf, al, ds1, ag1);
        gar1.setDataAccessRight(dar);
        gar1 = groupRepo.save(gar1);
        ag2 = new AccessGroup(ag2Name);
        ag2 = agRepo.save(ag2);
        gar2 = new GroupAccessRight(qf, al, ds2, ag2);
        gar2 = groupRepo.save(gar2);
        gar3 = new GroupAccessRight(qf, al, ds2, ag2);
    }

    @Test
    public void testRetrieveAccessRightsNoArgs() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS, expectations, ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRightsFullArgs() {
        final StringBuilder sb = new StringBuilder("?");
        sb.append("useremail=");
        sb.append(user1Email);
        sb.append("&accessgroup=");
        sb.append(ag1.getName());
        sb.append("&dataset=");
        sb.append(ds1.getIpId());
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb.toString(), expectations,
                          ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRightsUserArgs() {
        final StringBuilder sb = new StringBuilder("?");
        sb.append("useremail=");
        sb.append(user1Email);
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb.toString(), expectations,
                          ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRightsGroupArgs() {
        final StringBuilder sb = new StringBuilder("?");
        sb.append("accessgroup=");
        sb.append(ag1.getName());
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb.toString(), expectations,
                          ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRightsDSArgs() {
        final StringBuilder sb = new StringBuilder("?");
        sb.append("dataset=");
        sb.append(ds1.getIpId());
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb.toString(), expectations,
                          ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRightsUserAndDSArgs() {
        final StringBuilder sb = new StringBuilder("?");
        sb.append("useremail=");
        sb.append(user1Email);
        sb.append("&dataset=");
        sb.append(ds1.getIpId());
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb.toString(), expectations,
                          ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRightsUserAndGroupArgs() {
        final StringBuilder sb = new StringBuilder("?");
        sb.append("useremail=");
        sb.append(user1Email);
        sb.append("&accessgroup=");
        sb.append(ag1.getName());
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb.toString(), expectations,
                          ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRightsGroupAndDSArgs() {
        final StringBuilder sb = new StringBuilder("?");
        sb.append("dataset=");
        sb.append(ds1.getIpId());
        sb.append("&accessgroup=");
        sb.append(ag1.getName());
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb.toString(), expectations,
                          ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRight() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                          expectations, ACCESS_RIGHTS_ERROR_MSG, uar1.getId());
    }

    @Test
    public void testCreateAccessRight() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isCreated());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        // Associated dataset must be updated
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.dataset.groups[0]").value(ag2.getName()));
        performDefaultPost(AccessRightController.PATH_ACCESS_RIGHTS, gar3, expectations, ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testUpdateAccessRight() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        // Change access level
        UserAccessRight uarTmp = new UserAccessRight(qf, AccessLevel.RESTRICTED_ACCESS, ds1, user1);
        uarTmp.setId(uar1.getId());
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.accessLevel").value("RESTRICTED_ACCESS"));
        performDefaultPut(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                          uarTmp, expectations, ACCESS_RIGHTS_ERROR_MSG, uar1.getId());
    }

    @Test
    public void testUpdateGroupAccessRight() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        // Change access level and group (ag2 instead of ag1)
        GroupAccessRight garTmp = new GroupAccessRight(qf, AccessLevel.RESTRICTED_ACCESS, ds1, ag2);
        garTmp.setId(gar1.getId());
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.accessLevel").value("RESTRICTED_ACCESS"));
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.dataset.groups[0]").value(ag2.getName()));
        performDefaultPut(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                          garTmp, expectations, ACCESS_RIGHTS_ERROR_MSG, gar1.getId());

        // Save again garTmp (with ag1 as access group and FULL_ACCESS)
        garTmp = new GroupAccessRight(qf, AccessLevel.FULL_ACCESS, ds1, ag1);
        garTmp.setId(gar1.getId());
        expectations.clear();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.accessLevel").value("FULL_ACCESS"));
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.dataset.groups[0]").value(ag1.getName()));
        performDefaultPut(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                          garTmp, expectations, ACCESS_RIGHTS_ERROR_MSG, gar1.getId());
    }

    @Test
    public void testDeleteAccessRight() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());
        performDefaultDelete(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                             expectations, ACCESS_RIGHTS_ERROR_MSG, uar1.getId());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
