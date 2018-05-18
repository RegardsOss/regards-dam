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
package fr.cnes.regards.modules.dataaccess.client;

import javax.validation.Valid;
import java.util.List;

import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.cnes.regards.framework.feign.annotation.RestClient;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;

/**
 * Client of UserController
 * @author Sylvain Vissiere-Guerinet
 *
 */
@RestClient(name = "rs-dam")
@RequestMapping(value = IUserClient.BASE_PATH, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public interface IUserClient { // NOSONAR

    public static final String BASE_PATH = "/users/{email}/accessgroups";

    public static final String GROUP_NAME_PATH = "/{name}";

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<PagedResources<Resource<AccessGroup>>> retrieveAccessGroupsOfUser(
            @Valid @PathVariable("email") final String pUserEmail, @RequestParam("page") int pPage,
            @RequestParam("size") int pSize);

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> setAccessGroupsOfUser(@Valid @PathVariable("email") final String pUserEmail,
            final List<AccessGroup> pNewAcessGroups);

    @RequestMapping(method = RequestMethod.PUT, value = GROUP_NAME_PATH)
    @ResponseBody
    public ResponseEntity<Void> associateAccessGroupToUser(@Valid @PathVariable("email") final String pUserEmail,
            @Valid @PathVariable("name") final String pAcessGroupNameToBeAdded);

    @RequestMapping(method = RequestMethod.DELETE, value = GROUP_NAME_PATH)
    @ResponseBody
    public ResponseEntity<Void> dissociateAccessGroupFromUser(@Valid @PathVariable("email") final String pUserEmail,
            @Valid @PathVariable("name") final String pAcessGroupNameToBeAdded);
}
