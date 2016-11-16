/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.IFragmentService;
import fr.cnes.regards.modules.models.signature.IFragmentSignature;

/**
 * REST controller for managing {@link Fragment}
 *
 * @author Marc Sordi
 *
 */
@RestController
public class FragmentController implements IFragmentSignature, IResourceController<Fragment> {

    /**
     * Fragment service
     */
    private final IFragmentService fragmentService;

    /**
     * Resource service
     */
    private final IResourceService resourceService;

    public FragmentController(IFragmentService pFragmentService, IResourceService pResourceService) {
        this.fragmentService = pFragmentService;
        this.resourceService = pResourceService;
    }

    @Override
    @ResourceAccess(description = "List all fragments")
    public ResponseEntity<List<Resource<Fragment>>> getFragments() {
        return ResponseEntity.ok(toResources(fragmentService.getFragments()));
    }

    @Override
    @ResourceAccess(description = "Add a fragment")
    public ResponseEntity<Resource<Fragment>> addFragment(Fragment pFragment) throws ModuleException {
        return ResponseEntity.ok(toResource(fragmentService.addFragment(pFragment)));
    }

    @Override
    @ResourceAccess(description = "Get a fragment")
    public ResponseEntity<Resource<Fragment>> getFragment(Long pFragmentId) throws ModuleException {
        return ResponseEntity.ok(toResource(fragmentService.getFragment(pFragmentId)));
    }

    @Override
    @ResourceAccess(description = "Update a fragment")
    public ResponseEntity<Resource<Fragment>> updateFragment(Long pFragmentId, Fragment pFragment)
            throws ModuleException {
        return ResponseEntity.ok(toResource(fragmentService.updateFragment(pFragmentId, pFragment)));
    }

    @Override
    @ResourceAccess(description = "Delete a fragment")
    public ResponseEntity<Void> deleteFragment(Long pFragmentId) throws ModuleException {
        fragmentService.deleteFragment(pFragmentId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @ResourceAccess(description = "Export a fragment")
    public void exportFragment(HttpServletRequest pRequest, HttpServletResponse pResponse, Long pFragmentId) {
        // TODO Auto-generated method stub

    }

    @Override
    @ResourceAccess(description = "Import a fragment")
    public ResponseEntity<String> importFragment(MultipartFile pFile) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Resource<Fragment> toResource(Fragment pElement, Object... pExtras) {
        final Resource<Fragment> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, this.getClass(), "getFragment", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "updateFragment", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Fragment.class));
        resourceService.addLink(resource, this.getClass(), "deleteFragment", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "getFragments", LinkRels.LIST);
        // Import / Export
        resourceService.addLink(resource, this.getClass(), "exportFragment", "export",
                                MethodParamFactory.build(HttpServletRequest.class),
                                MethodParamFactory.build(HttpServletResponse.class),
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "importFragment", "import",
                                MethodParamFactory.build(MultipartFile.class));
        return resource;
    }
}
