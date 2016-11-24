/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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

/**
 * REST controller for managing {@link Fragment}
 *
 * @author Marc Sordi
 *
 */
@RestController
@RequestMapping(FragmentController.TYPE_MAPPING)
public class FragmentController implements IResourceController<Fragment> {

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/models/fragments";

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

    /**
     * Retrieve all fragments except default one
     *
     * @return list of fragments
     */
    @ResourceAccess(description = "List all fragments")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<Fragment>>> getFragments() {
        return ResponseEntity.ok(toResources(fragmentService.getFragments()));
    }

    /**
     * Create a new fragment
     *
     * @param pFragment
     *            the fragment to create
     * @return the created {@link Fragment}
     * @throws ModuleException
     *             if error occurs!
     */
    @ResourceAccess(description = "Add a fragment")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Resource<Fragment>> addFragment(@Valid @RequestBody Fragment pFragment)
            throws ModuleException {
        return ResponseEntity.ok(toResource(fragmentService.addFragment(pFragment)));
    }

    /**
     * Retrieve a fragment
     *
     * @param pFragmentId
     *            fragment identifier
     * @return the retrieved {@link Fragment}
     * @throws ModuleException
     *             if error occurs!
     */
    @ResourceAccess(description = "Get a fragment")
    @RequestMapping(method = RequestMethod.GET, value = "/{pFragmentId}")
    public ResponseEntity<Resource<Fragment>> getFragment(@PathVariable Long pFragmentId) throws ModuleException {
        return ResponseEntity.ok(toResource(fragmentService.getFragment(pFragmentId)));
    }

    /**
     * Update fragment. At the moment, only its description is updatable.
     *
     * @param pFragmentId
     *            fragment identifier
     * @param pFragment
     *            the fragment
     * @return the updated {@link Fragment}
     * @throws ModuleException
     *             if error occurs!
     */
    @ResourceAccess(description = "Update a fragment")
    @RequestMapping(method = RequestMethod.PUT, value = "/{pFragmentId}")
    public ResponseEntity<Resource<Fragment>> updateFragment(@PathVariable Long pFragmentId,
            @Valid @RequestBody Fragment pFragment) throws ModuleException {
        return ResponseEntity.ok(toResource(fragmentService.updateFragment(pFragmentId, pFragment)));
    }

    /**
     * Delete a fragment
     *
     * @param pFragmentId
     *            fragment identifier
     * @return nothing
     * @throws ModuleException
     *             if error occurs!
     */
    @ResourceAccess(description = "Delete a fragment")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pFragmentId}")
    public ResponseEntity<Void> deleteFragment(@PathVariable Long pFragmentId) throws ModuleException {
        fragmentService.deleteFragment(pFragmentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Export a model fragment
     *
     * @param pRequest
     *            HTTP request
     * @param pResponse
     *            HTTP response
     * @param pFragmentId
     *            fragment to export
     * @throws ModuleException
     *             if error occurs!
     */
    @ResourceAccess(description = "Export a fragment")
    // CHECKSTYLE:OFF
    @RequestMapping(method = RequestMethod.GET, value = "/{pFragmentId}/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    // CHECKSTYLE:ON
    public void exportFragment(HttpServletRequest pRequest, HttpServletResponse pResponse,
            @PathVariable Long pFragmentId) throws ModuleException {
        // TODO Auto-generated method stub

    }

    /**
     * Import a model fragment
     *
     * @param pFile
     *            file representing the fragment
     * @return TODO
     * @throws ModuleException
     *             if error occurs!
     */
    // TODO adapt signature / see Spring MVC doc p.22.10
    @ResourceAccess(description = "Import a fragment")
    @RequestMapping(method = RequestMethod.POST, value = "/import")
    public ResponseEntity<String> importFragment(@RequestParam("file") MultipartFile pFile) throws ModuleException {
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
