/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.rest.dataaccess;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Léo Mieulet
 */
@TestPropertySource(locations = { "classpath:test.properties" },
        properties = { "spring.jpa.properties.hibernate.default_schema=dam_ag_rest" })
public class AccessRightControllerIT extends AbstractAccessRightControllerIT {

    @Test
    public void testRetrieveAccessRightsNoArgs() {
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT), ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveDatasetWithAccessRights() {
        performDefaultGet(DatasetWithAccessRightController.ROOT_PATH + DatasetWithAccessRightController.GROUP_PATH,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT), ACCESS_RIGHTS_ERROR_MSG,
                          ag1Name);
    }

    @Test
    public void testRetrieveAccessRightsGroupArgs() {
        String sb = "?" + "accessgroup=" + ag1.getName();
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT), ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRightsDSArgs() {
        String sb = "?" + "dataset=" + ds1.getIpId();
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT), ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRightsFullArgs() {
        String sb = "?" + "dataset=" + ds1.getIpId() + "&accessgroup=" + ag1.getName();
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT), ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRight() {
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT), ACCESS_RIGHTS_ERROR_MSG,
                          ar1.getId());
    }

    @Test
    public void testCreateAccessRight() {
        // Associated dataset must be updated
        performDefaultPost(AccessRightController.PATH_ACCESS_RIGHTS, ar3, customizer().expectStatusCreated()
                .expectIsNotEmpty(JSON_PATH_ROOT).expectValue("$.content.dataset.groups[0]", ag2.getName()),
                           ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testIsUserAutorisedToAccessDataset() {

        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_IS_DATASET_ACCESSIBLE,
                          customizer().expectStatusOk().expect(MockMvcResultMatchers.content().string("true"))
                                  .addParameter("dataset", ds1.getIpId().toString()).addParameter("user", email),
                          ACCESS_RIGHTS_ERROR_MSG);

        String notExistingUser = "not.existing" + email;

        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_IS_DATASET_ACCESSIBLE,
                          customizer().expectStatusOk().expect(MockMvcResultMatchers.content().string("false"))
                                  .addParameter("dataset", ds1.getIpId().toString())
                                  .addParameter("user", notExistingUser),
                          ACCESS_RIGHTS_ERROR_MSG);
    }
}
