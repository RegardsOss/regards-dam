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

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.IFragmentService;
import fr.cnes.regards.modules.models.signature.IFragmentSignature;

/**
 * REST controller for managing model fragments
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
    public ResponseEntity<List<Resource<Fragment>>> getFragments() {
        return ResponseEntity.ok(toResources(fragmentService.getFragments()));
    }

    @Override
    public ResponseEntity<Resource<Fragment>> addFragment(Fragment pFragment) throws ModuleException {
        return ResponseEntity.ok(toResource(fragmentService.addFragment(pFragment)));
    }

    @Override
    public ResponseEntity<Resource<Fragment>> getFragment(Long pFragmentId) throws ModuleException {
        return ResponseEntity.ok(toResource(fragmentService.getFragment(pFragmentId)));
    }

    @Override
    public ResponseEntity<Resource<Fragment>> updateFragment(Long pFragmentId, Fragment pFragment)
            throws ModuleException {
        return ResponseEntity.ok(toResource(fragmentService.updateFragment(pFragmentId, pFragment)));
    }

    @Override
    public ResponseEntity<Void> deleteFragment(Long pFragmentId) throws ModuleException {
        fragmentService.deleteFragment(pFragmentId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public Resource<Fragment> toResource(Fragment pElement) {
        final Resource<Fragment> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, this.getClass(), "getAttribute", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        return resource;
    }

    @Override
    public void downloadFragment(HttpServletRequest pRequest, HttpServletResponse pResponse, Long pFragmentId) {
        // TODO Auto-generated method stub

    }

}
