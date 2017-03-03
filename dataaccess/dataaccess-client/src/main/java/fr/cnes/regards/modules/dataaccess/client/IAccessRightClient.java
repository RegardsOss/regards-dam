/*
 * LICENSE_PLACEHOLDER
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

import fr.cnes.regards.modules.dataaccess.domain.accessright.AbstractAccessRight;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

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
    public ResponseEntity<PagedResources<Resource<AbstractAccessRight>>> retrieveAccessRightsList(
            @RequestParam(name = "accessgroup", required = false) String pAccessGroupName,
            @RequestParam(name = "dataset", required = false) UniformResourceName pDatasetIpId,
            @RequestParam(name = "useremail", required = false) String pUserEmail, @RequestParam("page") int pPage,
            @RequestParam("size") int pSize);

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Resource<AbstractAccessRight>> createAccessRight(
            @Valid @RequestBody AbstractAccessRight pAccessRight);

    @RequestMapping(method = RequestMethod.GET, path = PATH_ACCESS_RIGHTS_ID)
    @ResponseBody
    public ResponseEntity<Resource<AbstractAccessRight>> retrieveAccessRight(
            @Valid @PathVariable("accessright_id") Long pId);

    @RequestMapping(method = RequestMethod.PUT, path = PATH_ACCESS_RIGHTS_ID)
    @ResponseBody
    public ResponseEntity<Resource<AbstractAccessRight>> updateAccessRight(
            @Valid @PathVariable("accessright_id") Long pId, @Valid AbstractAccessRight pToBe);

    @RequestMapping(method = RequestMethod.DELETE, path = PATH_ACCESS_RIGHTS_ID)
    @ResponseBody
    public ResponseEntity<Void> deleteAccessRight(@Valid @PathVariable("accessright_id") Long pId);

}
