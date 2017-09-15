/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@RestClient(name = "rs-dam")
@RequestMapping(value = IAccessRightClient.PATH_ACCESS_RIGHTS, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public interface IAccessRightClient { // NOSONAR

    public static final String PATH_ACCESS_RIGHTS = "/accessrights";

    public static final String PATH_ACCESS_RIGHTS_ID = "/{accessright_id}";

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<PagedResources<Resource<AccessRight>>> retrieveAccessRightsList(
            @RequestParam(name = "accessgroup", required = false) String pAccessGroupName,
            @RequestParam(name = "dataset", required = false) UniformResourceName pDatasetIpId,
            @RequestParam(name = "useremail", required = false) String pUserEmail, @RequestParam("page") int pPage,
            @RequestParam("size") int pSize);

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Resource<AccessRight>> createAccessRight(
            @Valid @RequestBody AccessRight pAccessRight);

    @RequestMapping(method = RequestMethod.GET, path = PATH_ACCESS_RIGHTS_ID)
    @ResponseBody
    public ResponseEntity<Resource<AccessRight>> retrieveAccessRight(
            @Valid @PathVariable("accessright_id") Long pId);

    @RequestMapping(method = RequestMethod.PUT, path = PATH_ACCESS_RIGHTS_ID)
    @ResponseBody
    public ResponseEntity<Resource<AccessRight>> updateAccessRight(
            @Valid @PathVariable("accessright_id") Long pId, @Valid AccessRight pToBe);

    @RequestMapping(method = RequestMethod.DELETE, path = PATH_ACCESS_RIGHTS_ID)
    @ResponseBody
    public ResponseEntity<Void> deleteAccessRight(@Valid @PathVariable("accessright_id") Long pId);

}
