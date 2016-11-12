/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.signature;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.ModelType;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 *
 * Model management API
 *
 * @author Marc Sordi
 *
 */
@RequestMapping("/models")
public interface IModelSignature {

    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<List<Resource<Model>>> getModels(@RequestParam(value = "type", required = false) ModelType pType);

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
     * Get all {@link ModelAttribute}
     *
     * @param pModelId
     *            {@link Model} identifier
     * @return list of linked {@link ModelAttribute}
     * @throws ModuleException
     *             if model unknown
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{pModelId}/attributes")
    ResponseEntity<List<Resource<ModelAttribute>>> getModelAttributes(@PathVariable Long pModelId)
            throws ModuleException;

    /**
     * Link an {@link AttributeModel} to a {@link Model} with a {@link ModelAttribute}.<br/>
     * This method is only available for {@link AttributeModel} in <b>default</b> {@link Fragment} (i.e. without name
     * space).
     *
     * @param pModelId
     *            {@link Model} identifier
     * @param pModelAttribute
     *            {@link ModelAttribute} to link
     * @return the {@link ModelAttribute} representing the link between the {@link Model} and the {@link AttributeModel}
     * @throws ModuleException
     *             if assignation cannot be done
     */
    @RequestMapping(method = RequestMethod.POST, value = "/{pModelId}/attributes")
    ResponseEntity<Resource<ModelAttribute>> linkAttributeToModel(@PathVariable Long pModelId,
            @Valid @RequestBody ModelAttribute pModelAttribute) throws ModuleException;

    /**
     * Retrieve a {@link ModelAttribute} linked to a {@link Model} id
     *
     * @param pModelId
     *            model identifier
     * @param pAttributeId
     *            attribute id
     * @return linked model attribute
     * @throws ModuleException
     *             if attribute cannot be retrieved
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{pModelId}/attributes/{pAttributeId}")
    ResponseEntity<Resource<ModelAttribute>> getModelAttribute(@PathVariable Long pModelId,
            @PathVariable Long pAttributeId) throws ModuleException;

    /**
     * Allow to update calculation properties
     *
     * @param pModelId
     *            model identifier
     * @param pAttributeId
     *            attribute id
     * @param pModelAttribute
     *            attribute
     * @return update model attribute
     * @throws ModuleException
     *             if attribute cannot be updated
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{pModelId}/attributes/{pAttributeId}")
    ResponseEntity<Resource<ModelAttribute>> getModelAttribute(@PathVariable Long pModelId,
            @PathVariable Long pAttributeId, @Valid @RequestBody ModelAttribute pModelAttribute) throws ModuleException;

    /**
     * Unlink a {@link ModelAttribute} from a {@link Model}.<br/>
     * This method is only available for {@link AttributeModel} in <b>default</b> {@link Fragment} (i.e. without
     * namespace).
     *
     * @param pModelId
     *            model identifier
     * @param pAttributeId
     *            attribute id
     * @return nothing
     * @throws ModuleException
     *             if attribute cannot be removed
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pModelId}/attributes/{pAttributeId}")
    ResponseEntity<Void> unlinkAttributeFromModel(@PathVariable Long pModelId, @PathVariable Long pAttributeId)
            throws ModuleException;

    /**
     * Link all {@link AttributeModel} of a particular {@link Fragment} to a model creating {@link ModelAttribute}.<br/>
     * This method is only available for {@link AttributeModel} in a <b>particular</b> {@link Fragment} (i.e. with name
     * space, not default one).
     *
     * @param pModelId
     *            model identifier
     * @param pFragmentName
     *            model attribute
     * @return linked model attributes
     * @throws ModuleException
     *             if assignation cannot be done
     */
    @RequestMapping(method = RequestMethod.POST, value = "/{pModelId}/attributes/fragments/{pFragmentName}")
    ResponseEntity<List<Resource<ModelAttribute>>> linkNSAttributeToModel(@PathVariable Long pModelId,
            @Pattern(regexp = Fragment.FRAGMENT_NAME_REGEXP) @PathVariable String pFragmentName) throws ModuleException;

    /**
     * Unlink all {@link AttributeModel} of a particular {@link Fragment} from a model deleting all associated
     * {@link ModelAttribute}.<br/>
     * This method is only available for {@link AttributeModel} in a <b>particular</b> {@link Fragment} (i.e. with name
     * space, not default one).
     *
     * @param pModelId
     *            model identifier
     * @param pFragmentName
     *            model attribute
     * @return linked model attributes
     * @throws ModuleException
     *             if assignation cannot be done
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pModelId}/attributes/fragments/{pFragmentName}")
    ResponseEntity<Void> unlinkNSAttributeToModel(@PathVariable Long pModelId,
            @Pattern(regexp = Fragment.FRAGMENT_NAME_REGEXP) @PathVariable String pFragmentName) throws ModuleException;

    /**
     * Download the model
     *
     * @param pRequest
     *            HTTP request
     * @param pResponse
     *            HTTP response
     * @param pModelId
     *            model to download
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{pModelId}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadModel(HttpServletRequest pRequest, HttpServletResponse pResponse, @PathVariable Long pModelId);

    // TODO : model upload / see Spring MVC doc p.22.10
}
