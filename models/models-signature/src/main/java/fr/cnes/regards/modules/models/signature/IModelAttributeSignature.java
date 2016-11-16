/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.signature;

import java.util.List;

import javax.validation.Valid;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 *
 * {@link ModelAttribute} management API
 *
 * @author Marc Sordi
 *
 */
@RequestMapping("/models/{pModelId}/attributes")
public interface IModelAttributeSignature {

    /**
     * Get all {@link ModelAttribute}
     *
     * @param pModelId
     *            {@link Model} identifier
     * @return list of linked {@link ModelAttribute}
     * @throws ModuleException
     *             if model unknown
     */
    @RequestMapping(method = RequestMethod.GET)
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
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Resource<ModelAttribute>> bindAttributeToModel(@PathVariable Long pModelId,
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
    @RequestMapping(method = RequestMethod.GET, value = "/{pAttributeId}")
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
    @RequestMapping(method = RequestMethod.PUT, value = "/{pAttributeId}")
    ResponseEntity<Resource<ModelAttribute>> updateModelAttribute(@PathVariable Long pModelId,
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/{pAttributeId}")
    ResponseEntity<Void> unbindAttributeFromModel(@PathVariable Long pModelId, @PathVariable Long pAttributeId)
            throws ModuleException;

    /**
     * Link all {@link AttributeModel} of a particular {@link Fragment} to a model creating {@link ModelAttribute}.<br/>
     * This method is only available for {@link AttributeModel} in a <b>particular</b> {@link Fragment} (i.e. with name
     * space, not default one).
     *
     * @param pModelId
     *            model identifier
     * @param pFragmentId
     *            fragment identifier
     * @return linked model attributes
     * @throws ModuleException
     *             if binding cannot be done
     */
    @RequestMapping(method = RequestMethod.POST, value = "/fragments/{pFragmentId}")
    ResponseEntity<List<Resource<ModelAttribute>>> bindNSAttributeToModel(@PathVariable Long pModelId,
            @PathVariable Long pFragmentId) throws ModuleException;

    /**
     * Unlink all {@link AttributeModel} of a particular {@link Fragment} from a model deleting all associated
     * {@link ModelAttribute}.<br/>
     * This method is only available for {@link AttributeModel} in a <b>particular</b> {@link Fragment} (i.e. with name
     * space, not default one).
     *
     * @param pModelId
     *            model identifier
     * @param pFragmentId
     *            fragment identifier
     * @return linked model attributes
     * @throws ModuleException
     *             if binding cannot be done
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/fragments/{pFragmentId}")
    ResponseEntity<Void> unbindNSAttributeFromModel(@PathVariable Long pModelId, @PathVariable Long pFragmentId)
            throws ModuleException;
}
