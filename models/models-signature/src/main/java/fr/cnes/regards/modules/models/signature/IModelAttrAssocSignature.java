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
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 *
 * {@link ModelAttrAssoc} management API
 *
 * @author Marc Sordi
 *
 */
@RequestMapping("/models/{pModelId}/attributes")
public interface IModelAttrAssocSignature {

    /**
     * Get all {@link ModelAttrAssoc}
     *
     * @param pModelId
     *            {@link Model} identifier
     * @return list of linked {@link ModelAttrAssoc}
     * @throws ModuleException
     *             if model unknown
     */
    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<List<Resource<ModelAttrAssoc>>> getModelAttrAssocs(@PathVariable Long pModelId)
            throws ModuleException;

    /**
     * Link an {@link AttributeModel} to a {@link Model} with a {@link ModelAttrAssoc}.<br/>
     * This method is only available for {@link AttributeModel} in <b>default</b> {@link Fragment} (i.e. without name
     * space).
     *
     * @param pModelId
     *            {@link Model} identifier
     * @param pModelAttribute
     *            {@link ModelAttrAssoc} to link
     * @return the {@link ModelAttrAssoc} representing the link between the {@link Model} and the {@link AttributeModel}
     * @throws ModuleException
     *             if assignation cannot be done
     */
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Resource<ModelAttrAssoc>> bindAttributeToModel(@PathVariable Long pModelId,
            @Valid @RequestBody ModelAttrAssoc pModelAttribute) throws ModuleException;

    /**
     * Retrieve a {@link ModelAttrAssoc} linked to a {@link Model} id
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
    ResponseEntity<Resource<ModelAttrAssoc>> getModelAttrAssoc(@PathVariable Long pModelId,
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
    ResponseEntity<Resource<ModelAttrAssoc>> updateModelAttrAssoc(@PathVariable Long pModelId,
            @PathVariable Long pAttributeId, @Valid @RequestBody ModelAttrAssoc pModelAttribute) throws ModuleException;

    /**
     * Unlink a {@link ModelAttrAssoc} from a {@link Model}.<br/>
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
     * Link all {@link AttributeModel} of a particular {@link Fragment} to a model creating {@link ModelAttrAssoc}.<br/>
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
    ResponseEntity<List<Resource<ModelAttrAssoc>>> bindNSAttributeToModel(@PathVariable Long pModelId,
            @PathVariable Long pFragmentId) throws ModuleException;

    /**
     * Unlink all {@link AttributeModel} of a particular {@link Fragment} from a model deleting all associated
     * {@link ModelAttrAssoc}.<br/>
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
