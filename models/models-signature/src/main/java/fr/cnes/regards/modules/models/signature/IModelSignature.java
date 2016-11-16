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
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.ModelType;

/**
 *
 * {@link Model} management API
 *
 * @author Marc Sordi
 *
 */
@RequestMapping("/models")
public interface IModelSignature {

    /**
     * Retrieve all {@link Model}. The request can be filtered by {@link ModelType}.
     *
     * @param pType
     *            filter
     * @return a list of {@link Model}
     */
    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<List<Resource<Model>>> getModels(@RequestParam(value = "type", required = false) ModelType pType);

    /**
     * Create a {@link Model}
     *
     * @param pModel
     *            the {@link Model} to create
     * @return the created {@link Model}
     * @throws ModuleException
     *             if problem occurs during model creation
     */
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Resource<Model>> createModel(@Valid @RequestBody Model pModel) throws ModuleException;

    /**
     * Get a {@link Model} without its attributes
     *
     * @param pModelId
     *            {@link Model} identifier
     * @return a {@link Model}
     * @throws ModuleException
     *             if model cannot be retrieved
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{pModelId}")
    ResponseEntity<Resource<Model>> getModel(@PathVariable Long pModelId) throws ModuleException;

    /**
     * Allow to update {@link Model} description
     *
     * @param pModelId
     *            {@link Model} identifier
     * @param pModel
     *            {@link Model} to update
     * @return updated {@link Model}
     * @throws ModuleException
     *             if model cannot be updated
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{pModelId}")
    ResponseEntity<Resource<Model>> updateModel(@PathVariable Long pModelId, @Valid @RequestBody Model pModel)
            throws ModuleException;

    /**
     * Delete a {@link Model} and detach all {@link ModelAttribute}
     *
     * @param pModelId
     *            {@link Model} identifier
     * @return nothing
     * @throws ModuleException
     *             if model cannot be deleted
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pModelId}")
    ResponseEntity<Void> deleteModel(@PathVariable Long pModelId) throws ModuleException;

    /**
     * Duplicate a model
     *
     * @param pModelId
     *            {@link Model} to duplicate
     * @param pModel
     *            new model to create with its own name and description (other informations are skipped)
     * @return a new model based on actual one
     * @throws ModuleException
     *             if error occurs!
     */
    @RequestMapping(method = RequestMethod.POST, value = "/{pModelId}/duplicate")
    ResponseEntity<Resource<Model>> duplicateModel(@PathVariable Long pModelId, @Valid @RequestBody Model pModel)
            throws ModuleException;

    /**
     * Export a model
     *
     * @param pRequest
     *            HTTP request
     * @param pResponse
     *            HTTP response
     * @param pModelId
     *            model to export
     * @throws ModuleException
     *             if error occurs!
     */
    // CHECKSTYLE:OFF
    @RequestMapping(method = RequestMethod.GET, value = "/{pModelId}/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    // CHECKSTYLE:ON
    public void exportModel(HttpServletRequest pRequest, HttpServletResponse pResponse, @PathVariable Long pModelId)
            throws ModuleException;

    /**
     * Import a model
     *
     * @param pFile
     *            model to import
     * @return TODO
     * @throws ModuleException
     *             if error occurs!
     */
    // TODO adapt signature / see Spring MVC doc p.22.10
    @RequestMapping(method = RequestMethod.POST, value = "/import")
    public ResponseEntity<String> importModel(@RequestParam("file") MultipartFile pFile) throws ModuleException;
}
