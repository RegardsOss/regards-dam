/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.FragmentService;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
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
     * Prefix for imported/exported filename
     */
    private static final String FRAGMENT_FILE_PREFIX = "fragment-";

    /**
     * Suffix for imported/exported filename
     */
    private static final String FRAGMENT_EXTENSION = ".xml";

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentService.class);

    /**
     * Fragment service
     */
    private final IFragmentService fragmentService;

    /**
     * Resource service
     */
    private final IResourceService resourceService;

    private final IAttributeModelService attributeModelService;

    public FragmentController(IFragmentService pFragmentService, IResourceService pResourceService,
            IAttributeModelService attributeModelService) {
        this.fragmentService = pFragmentService;
        this.resourceService = pResourceService;
        this.attributeModelService = attributeModelService;
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
    @RequestMapping(method = RequestMethod.GET, value = "/{pFragmentId}/export")
    public void exportFragment(HttpServletRequest pRequest, HttpServletResponse pResponse,
            @PathVariable Long pFragmentId) throws ModuleException {

        final Fragment fragment = fragmentService.getFragment(pFragmentId);
        final String exportedFilename = FRAGMENT_FILE_PREFIX + fragment.getName() + FRAGMENT_EXTENSION;

        // Produce octet stream to force navigator opening "save as" dialog
        pResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        pResponse.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportedFilename + "\"");

        try {
            fragmentService.exportFragment(pFragmentId, pResponse.getOutputStream());
            pResponse.getOutputStream().flush();
        } catch (IOException e) {
            final String message = String
                    .format("Error with servlet output stream while exporting fragment %s.", fragment.getName());
            LOGGER.error(message, e);
            throw new ModuleException(e);
        }
    }

    /**
     * Import a model fragment
     *
     * @param pFile
     *            file representing the fragment
     * @return nothing
     * @throws ModuleException
     *             if error occurs!
     */
    @ResourceAccess(description = "Import a fragment")
    @RequestMapping(method = RequestMethod.POST, value = "/import")
    public ResponseEntity<Resource<Fragment>> importFragment(@RequestParam("file") MultipartFile pFile)
            throws ModuleException {
        try {
            Fragment frag = fragmentService.importFragment(pFile.getInputStream());
            return new ResponseEntity<>(toResource(frag), HttpStatus.CREATED);
        } catch (IOException e) {
            final String message = "Error with file stream while importing fragment.";
            LOGGER.error(message, e);
            throw new ModuleException(e);
        }
    }

    @Override
    public Resource<Fragment> toResource(Fragment fragment, Object... pExtras) {
        final Resource<Fragment> resource = resourceService.toResource(fragment);
        resourceService.addLink(resource, this.getClass(), "getFragment", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, fragment.getId()));
        resourceService.addLink(resource, this.getClass(), "updateFragment", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, fragment.getId()),
                                MethodParamFactory.build(Fragment.class));
        if (isDeletable(fragment)) {
            resourceService.addLink(resource, this.getClass(), "deleteFragment", LinkRels.DELETE,
                                    MethodParamFactory.build(Long.class, fragment.getId()));
        }
        resourceService.addLink(resource, this.getClass(), "getFragments", LinkRels.LIST);
        // Export
        resourceService.addLink(resource, this.getClass(), "exportFragment", "export",
                                MethodParamFactory.build(HttpServletRequest.class),
                                MethodParamFactory.build(HttpServletResponse.class),
                                MethodParamFactory.build(Long.class, fragment.getId()));
        return resource;
    }

    private boolean isDeletable(Fragment fragment) {
        try {
            List<AttributeModel> fragmentAttributes = attributeModelService.findByFragmentId(fragment.getId());
            return fragmentAttributes.isEmpty();
        } catch (ModuleException e) {
            // This exception cannot happens as we are using this method after this fragment has already been retrieve.
            // Anyway, if the fragment doesn't exist, it is not deletable
            return false;
        }
    }
}
