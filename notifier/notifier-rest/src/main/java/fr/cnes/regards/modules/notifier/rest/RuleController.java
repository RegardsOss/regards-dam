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
import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.regards.modules.notifier.service.IRuleService;
import fr.cnes.reguards.modules.notifier.dto.RuleDto;

/**
 * REST interface for managing data {@link Rule}
 * @author kevin marchois
 *
 */
@RestController
@RequestMapping(RuleController.RULE)
public class RuleController implements IResourceController<RuleDto> {

    public static final String RULE = "/rule";

    public static final String ID = "/{id}";

    @Autowired
    private IRuleService ruleService;

    @Autowired
    private IResourceService resourceService;

    /**
     * Get all {@link Rule} from database the result will be paginated and transformed to {@link RuleDto}
     * @param page
     * @param assembler
     * @return
     */
    @ResourceAccess(description = "List all rules")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<PagedResources<Resource<RuleDto>>> getRules(Pageable page,
            final PagedResourcesAssembler<RuleDto> assembler) {
        return ResponseEntity.ok(toPagedResources(this.ruleService.getRules(page), assembler));
    }

    /**
     * Create a {@link Rule}
     * @return the created {@link Rule}
     */
    @ResourceAccess(description = "Create a rule")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Resource<RuleDto>> createRule(@Valid @RequestBody RuleDto toCreate) {
        return ResponseEntity.ok(toResource(this.ruleService.createOrUpdateRule(toCreate)));
    }

    /**
     * Update a {@link Rule}
     * @return the updated {@link Rule}
     */
    @ResourceAccess(description = "Update a recipient")
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<Resource<RuleDto>> updateRule(@Valid @RequestBody RuleDto toUpdate) {
        return ResponseEntity.ok(toResource(this.ruleService.createOrUpdateRule(toUpdate)));
    }

    /**
     * Delete a {@link Recipient}
     */
    @ResourceAccess(description = "Delete a rule")
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteRecipient(@PathVariable("id") Long id) {
        this.ruleService.deleteRule(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public Resource<RuleDto> toResource(RuleDto element, Object... extras) {

        Resource<RuleDto> resource = resourceService.toResource(element);
        resourceService.addLink(resource, this.getClass(), "getRules", LinkRels.SELF,
                                MethodParamFactory.build(Pageable.class));
        resourceService.addLink(resource, this.getClass(), "createRule", LinkRels.CREATE,
                                MethodParamFactory.build(Pageable.class));
        resourceService.addLink(resource, this.getClass(), "updateRule", LinkRels.UPDATE,
                                MethodParamFactory.build(Pageable.class));
        resourceService.addLink(resource, this.getClass(), "deleteRule", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, element.getId()));
        return resource;
    }
}
