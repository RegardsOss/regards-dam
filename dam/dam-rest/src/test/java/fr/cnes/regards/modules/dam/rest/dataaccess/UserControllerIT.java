/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.User;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@MultitenantTransactional
@TestPropertySource("classpath:test.properties")
public class UserControllerIT extends AbstractRegardsTransactionalIT {

    private static final Logger LOG = LoggerFactory.getLogger(UserControllerIT.class);

    private static final String USER_ERROR_MSG = "the response body should not be empty and status should be 200";

    private User USER1;

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_810")
    public void testRetrieveAccessGroupsListOfUser() {
        USER1 = new User("user1@user1.user1");
        performDefaultGet(UserController.BASE_PATH, customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT),
                          USER_ERROR_MSG, USER1.getEmail());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
