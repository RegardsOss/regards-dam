/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.rest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import fr.cnes.regards.modules.dataaccess.dao.IAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessright.*;
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
    private IModelRepository modelRepo;

    @Autowired
    private IDatasetRepository dsRepo;

    @Autowired
    private IAccessGroupRepository agRepo;

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

    private final String dsDesc = "http://test.test";

    private AccessRight ar1;

    private AccessRight ar2;

    private AccessRight ar3;

    @Autowired
    private AccessGroupService agService;

    @Autowired
    private IAccessRightRepository arRepo;

    @Before
    public void init() {
        OffsetDateTime now = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
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
        ds1.setCreationDate(now);
        ds1 = dsRepo.save(ds1);
        ds2 = new Dataset(model, "PROJECT", ds2Name);
        ds2.setLicence("licence");
        ds2.setCreationDate(now);
        ds2 = dsRepo.save(ds2);

        ag1 = new AccessGroup(ag1Name);
        ag1 = agRepo.save(ag1);
        ar1 = new AccessRight(qf, al, ds1, ag1);
        ar1.setDataAccessRight(dar);
        ar1 = arRepo.save(ar1);
        ag2 = new AccessGroup(ag2Name);
        ag2 = agRepo.save(ag2);
        ar2 = new AccessRight(qf, al, ds2, ag2);
        ar2 = arRepo.save(ar2);
        ar3 = new AccessRight(qf, al, ds2, ag2);
    }

    @Test
    public void testRetrieveAccessRightsNoArgs() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS, expectations, ACCESS_RIGHTS_ERROR_MSG);
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
    public void testRetrieveAccessRightsFullArgs() {
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
                          expectations, ACCESS_RIGHTS_ERROR_MSG, ar1.getId());
    }

    @Test
    public void testCreateAccessRight() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isCreated());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        // Associated dataset must be updated
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.dataset.groups[0]").value(ag2.getName()));
        performDefaultPost(AccessRightController.PATH_ACCESS_RIGHTS, ar3, expectations, ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testUpdateAccessRight() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        // Change access level and group (ag2 instead of ag1)
        AccessRight garTmp = new AccessRight(qf, AccessLevel.RESTRICTED_ACCESS, ds1, ag2);
        garTmp.setId(ar1.getId());
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.accessLevel").value("RESTRICTED_ACCESS"));
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.dataset.groups[0]").value(ag2.getName()));
        performDefaultPut(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                          garTmp, expectations, ACCESS_RIGHTS_ERROR_MSG, ar1.getId());

        // Save again garTmp (with ag1 as access group and FULL_ACCESS)
        garTmp = new AccessRight(qf, AccessLevel.FULL_ACCESS, ds1, ag1);
        garTmp.setId(ar1.getId());
        expectations.clear();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.accessLevel").value("FULL_ACCESS"));
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.dataset.groups[0]").value(ag1.getName()));
        performDefaultPut(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                          garTmp, expectations, ACCESS_RIGHTS_ERROR_MSG, ar1.getId());
    }

    @Test
    public void testDeleteAccessRight() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());
        performDefaultDelete(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                             expectations, ACCESS_RIGHTS_ERROR_MSG, ar1.getId());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
