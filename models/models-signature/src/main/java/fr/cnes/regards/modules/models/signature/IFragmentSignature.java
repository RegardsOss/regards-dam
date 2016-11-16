/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.signature;

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
import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 *
 * {@link Fragment} management API
 *
 * @author Marc Sordi
 *
 */
@RequestMapping("/models/fragments")
public interface IFragmentSignature {

    /**
     * Retrieve all fragments except default one
     *
     * @return list of fragments
     */
    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<List<Resource<Fragment>>> getFragments();

    /**
     * Create a new fragment
     *
     * @param pFragment
     *            the fragment to create
     * @return the created {@link Fragment}
     * @throws ModuleException
     *             if error occurs!
     */
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Resource<Fragment>> addFragment(@Valid @RequestBody Fragment pFragment) throws ModuleException;

    /**
     * Retrieve a fragment
     *
     * @param pFragmentId
     *            fragment identifier
     * @return the retrieved {@link Fragment}
     * @throws ModuleException
     *             if error occurs!
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{pFragmentId}")
    ResponseEntity<Resource<Fragment>> getFragment(@PathVariable Long pFragmentId) throws ModuleException;

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
    @RequestMapping(method = RequestMethod.PUT, value = "/{pFragmentId}")
    ResponseEntity<Resource<Fragment>> updateFragment(@PathVariable Long pFragmentId,
            @Valid @RequestBody Fragment pFragment) throws ModuleException;

    /**
     * Delete a fragment
     *
     * @param pFragmentId
     *            fragment identifier
     * @return nothing
     * @throws ModuleException
     *             if error occurs!
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pFragmentId}")
    ResponseEntity<Void> deleteFragment(@PathVariable Long pFragmentId) throws ModuleException;

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
    // CHECKSTYLE:OFF
    @RequestMapping(method = RequestMethod.GET, value = "/{pFragmentId}/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    // CHECKSTYLE:ON
    public void exportFragment(HttpServletRequest pRequest, HttpServletResponse pResponse,
            @PathVariable Long pFragmentId) throws ModuleException;

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
    @RequestMapping(method = RequestMethod.POST, value = "/import")
    public ResponseEntity<String> importFragment(@RequestParam("file") MultipartFile pFile) throws ModuleException;
}
