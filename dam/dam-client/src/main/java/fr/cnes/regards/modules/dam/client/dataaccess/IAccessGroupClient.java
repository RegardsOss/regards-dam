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
package fr.cnes.regards.modules.dam.client.dataaccess;

import javax.validation.Valid;

import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.cnes.regards.framework.feign.annotation.RestClient;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@RestClient(name = "rs-dam", contextId = "rs-dam.access-group.client")
@RequestMapping(value = IAccessGroupClient.PATH_ACCESS_GROUPS, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public interface IAccessGroupClient { // NOSONAR

    /**
     * Client base path
     */
    String PATH_ACCESS_GROUPS = "/accessgroups";

    /**
     * Client path using name as path variable
     */
    String PATH_ACCESS_GROUPS_NAME = "/{name}";

    /**
     * Client path user name and email as path variable
     */
    String PATH_ACCESS_GROUPS_NAME_EMAIL = PATH_ACCESS_GROUPS_NAME + "/{email}";

    /**
     * Retrieve access group
     * @param isPublic whether we are also looking for public groups
     * @param page which page
     * @param size which page size
     * @return a page of access group
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<PagedResources<Resource<AccessGroup>>> retrieveAccessGroupsList(
            @RequestParam(name = "public", required = false) Boolean isPublic, @RequestParam("page") int page,
            @RequestParam("size") int size);

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<Resource<AccessGroup>> createAccessGroup(@Valid @RequestBody AccessGroup toBeCreated);

    /**
     * Retrieve an access group by its name
     * @return the retrieved access group
     */
    @RequestMapping(method = RequestMethod.GET, path = PATH_ACCESS_GROUPS_NAME)
    @ResponseBody
    ResponseEntity<Resource<AccessGroup>> retrieveAccessGroup(@Valid @PathVariable("name") String groupName);

    /**
     * Delete an access group by its name
     */
    @RequestMapping(method = RequestMethod.DELETE, path = PATH_ACCESS_GROUPS_NAME)
    @ResponseBody
    ResponseEntity<Void> deleteAccessGroup(@Valid @PathVariable("name") String groupName);

    /**
     * Associate a user, represented by its email, to an access group, represented by its name.
     * @return the updated access group
     */
    @RequestMapping(method = RequestMethod.PUT, path = PATH_ACCESS_GROUPS_NAME_EMAIL)
    @ResponseBody
    ResponseEntity<Resource<AccessGroup>> associateUserToAccessGroup(@Valid @PathVariable("name") String groupName,
            @Valid @PathVariable("email") String userEmail);

    /**
     * Dissociate a user, represented by its email, from an access group, represented by its name.
     * @return the updated access group
     */
    @RequestMapping(method = RequestMethod.DELETE, path = PATH_ACCESS_GROUPS_NAME_EMAIL)
    @ResponseBody
    ResponseEntity<Resource<AccessGroup>> dissociateUserFromAccessGroup(@Valid @PathVariable("name") String groupName,
            @Valid @PathVariable("email") String userEmail);
}
