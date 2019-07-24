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

import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessLevel;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessRight;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Léo Mieulet
 */
@TestPropertySource(locations = "classpath:test.properties",
        properties = { "spring.jpa.properties.hibernate.default_schema=projectdbdelete" })
public class AccessRightControllerNoTxIT extends AbstractAccessRightControllerIT {

    @Test
    public void testUpdateAccessRight() {
        // Change access level
        AccessRight garTmp = new AccessRight(qf, AccessLevel.RESTRICTED_ACCESS, ds1, ag1);
        garTmp.setId(ar1.getId());

        performDefaultPut(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                          garTmp,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT)
                                  .expectValue("$.content.accessLevel", "RESTRICTED_ACCESS"),
                          ACCESS_RIGHTS_ERROR_MSG, ar1.getId());

        // Save again garTmp (with ag1 as access group and FULL_ACCESS)
        garTmp.setAccessLevel(AccessLevel.FULL_ACCESS);
        performDefaultPut(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                          garTmp,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT)
                                  .expectValue("$.content.accessLevel", "FULL_ACCESS"),
                          ACCESS_RIGHTS_ERROR_MSG, ar1.getId());

        // Delete access right
        performDefaultDelete(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                             customizer().expectStatusNoContent(), ACCESS_RIGHTS_ERROR_MSG, ar1.getId());
    }
}
