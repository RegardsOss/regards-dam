/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.rest;

import java.beans.PropertyEditorSupport;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.amqp.exception.RabbitMQVhostException;
import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AbstractAccessRight;
import fr.cnes.regards.modules.dataaccess.service.AccessRightService;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@RestController
@RequestMapping(AccessRightController.PATH_ACCESS_RIGHTS)
public class AccessRightController implements IResourceController<AbstractAccessRight> {

    public static final String PATH_ACCESS_RIGHTS = "/accessrights";

    public static final String PATH_ACCESS_RIGHTS_ID = "/{accessright_id}";

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private AccessRightService accessRightService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @ResourceAccess(description = "send the list, or subset asked, of accessRight")
    public ResponseEntity<PagedResources<Resource<AbstractAccessRight>>> retrieveAccessRightsList(
            @RequestParam(name = "accessgroup", required = false) String pAccessGroupName,
            @RequestParam(name = "dataset", required = false) UniformResourceName pDataSetIpId,
            @RequestParam(name = "useremail", required = false) String pUserEmail, final Pageable pPageable,
            final PagedResourcesAssembler<AbstractAccessRight> pAssembler) throws EntityNotFoundException {
        Page<AbstractAccessRight> accessRights = accessRightService.retrieveAccessRights(pAccessGroupName, pDataSetIpId,
                                                                                         pUserEmail, pPageable);
        return new ResponseEntity<>(toPagedResources(accessRights, pAssembler), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @ResourceAccess(description = "create an accessRight according to the argument")
    public ResponseEntity<Resource<AbstractAccessRight>> createAccessRight(
            @Valid @RequestBody AbstractAccessRight pAccessRight)
            throws EntityNotFoundException, RabbitMQVhostException {
        AbstractAccessRight created = accessRightService.createAccessRight(pAccessRight);
        return new ResponseEntity<>(toResource(created), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = PATH_ACCESS_RIGHTS_ID)
    @ResponseBody
    @ResourceAccess(description = "send the access right of id requested")
    public ResponseEntity<Resource<AbstractAccessRight>> retrieveAccessRight(
            @Valid @PathVariable("accessright_id") Long pId) throws EntityNotFoundException {
        AbstractAccessRight requested = accessRightService.retrieveAccessRight(pId);
        return new ResponseEntity<>(toResource(requested), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = PATH_ACCESS_RIGHTS_ID)
    @ResponseBody
    @ResourceAccess(description = "modify the access right of id requested according to the argument")
    public ResponseEntity<Resource<AbstractAccessRight>> updateAccessRight(
            @Valid @PathVariable("accessright_id") Long pId, @Valid AbstractAccessRight pToBe)
            throws EntityNotFoundException, EntityInconsistentIdentifierException, RabbitMQVhostException {
        AbstractAccessRight updated = accessRightService.updateAccessRight(pId, pToBe);
        return new ResponseEntity<>(toResource(updated), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = PATH_ACCESS_RIGHTS_ID)
    @ResponseBody
    @ResourceAccess(description = "delete the access right of id requested")
    public ResponseEntity<Void> deleteAccessRight(@Valid @PathVariable("accessright_id") Long pId)
            throws RabbitMQVhostException {
        accessRightService.deleteAccessRight(pId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public Resource<AbstractAccessRight> toResource(AbstractAccessRight pElement, Object... pExtras) {
        Resource<AbstractAccessRight> resource = new Resource<>(pElement);
        resourceService.addLink(resource, this.getClass(), "createAccessRight", LinkRels.CREATE,
                                MethodParamFactory.build(AbstractAccessRight.class, pElement));
        resourceService.addLink(resource, this.getClass(), "deleteAccessRight", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "updateAccessRight", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(AbstractAccessRight.class, pElement));
        resourceService.addLink(resource, this.getClass(), "retrieveAccessRight", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        return resource;
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(UniformResourceName.class, new PropertyEditorSupport() {

            Object value;

            @Override
            public Object getValue() {
                return value;
            }

            @Override
            public void setAsText(String text) {
                value = UniformResourceName.fromString(text);
            }
        });
    }

}
