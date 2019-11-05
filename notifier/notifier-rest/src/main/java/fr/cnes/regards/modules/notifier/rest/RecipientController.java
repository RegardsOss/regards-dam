/**
 *
 */
package fr.cnes.regards.modules.notifier.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.service.IRecipientService;
import fr.cnes.reguards.modules.notifier.dto.RecipientDto;

/**
 * REST interface for managing data {@link Recipient}
 * @author kevin marchois
 *
 */
@RestController
@RequestMapping(RecipientController.RECIPIENT)
public class RecipientController implements IResourceController<RecipientDto> {

    public static final String RECIPIENT = "/recipient";

    public static final String ID = "/{id}";

    @Autowired
    private IRecipientService recipientServcie;

    @Autowired
    private IResourceService resourceService;

    /**
     * Get all {@link Recipient} from database the result will be paginated and transformed to {@link RecipientDto}
     * @param page
     * @param assembler
     * @return
     */
    @ResourceAccess(description = "List all recipient")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<PagedResources<Resource<RecipientDto>>> getRecipients(Pageable page,
            final PagedResourcesAssembler<RecipientDto> assembler) {
        return ResponseEntity.ok(toPagedResources(this.recipientServcie.getRecipients(page), assembler));
    }

    /**
     * Create a {@link Recipient}
     * @return the created {@link Recipient}
     */
    @ResourceAccess(description = "Create a recipient")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Resource<RecipientDto>> createRecipient(@Valid @RequestBody RecipientDto toCreate) {
        return ResponseEntity.ok(toResource(this.recipientServcie.createOrUpdateRecipient(toCreate)));
    }

    /**
     * Update a {@link Recipient}
     * @return the updated {@link Recipient}
     */
    @ResourceAccess(description = "Update a recipient")
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<Resource<RecipientDto>> updateRecipient(@Valid @RequestBody RecipientDto toUpdate) {
        return ResponseEntity.ok(toResource(this.recipientServcie.createOrUpdateRecipient(toUpdate)));
    }

    /**
     * Delete a {@link Recipient}
     */
    @ResourceAccess(description = "Delete a recipient")
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteRecipient(@PathVariable("id") Long id) {
        this.recipientServcie.deleteRecipient(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public Resource<RecipientDto> toResource(RecipientDto element, Object... extras) {

        Resource<RecipientDto> resource = resourceService.toResource(element);
        resourceService.addLink(resource, this.getClass(), "getRecipients", LinkRels.SELF,
                                MethodParamFactory.build(Pageable.class));
        resourceService.addLink(resource, this.getClass(), "createRecipient", LinkRels.CREATE,
                                MethodParamFactory.build(Pageable.class));
        resourceService.addLink(resource, this.getClass(), "updateRecipient", LinkRels.UPDATE,
                                MethodParamFactory.build(Pageable.class));
        resourceService.addLink(resource, this.getClass(), "deleteRecipient", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, element.getId()));
        return resource;
    }
}
