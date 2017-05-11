/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;
import java.util.Set;

import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * Model attribute association service description
 *
 * @author Marc Sordi
 * @author oroussel
 */
public interface IModelAttrAssocService {

    List<ModelAttrAssoc> getModelAttrAssocs(Long pModelId) throws ModuleException;

    ModelAttrAssoc bindAttributeToModel(Long pModelId, ModelAttrAssoc pModelAttribute) throws ModuleException;

    ModelAttrAssoc getModelAttrAssoc(Long pModelId, Long pAttributeId) throws ModuleException;

    ModelAttrAssoc getModelAttrAssoc(Long pModelId, AttributeModel pAttribute);

    ModelAttrAssoc updateModelAttribute(Long pModelId, Long pAttributeId, ModelAttrAssoc pModelAttribute)
            throws ModuleException;

    void unbindAttributeFromModel(Long pModelId, Long pAttributeId) throws ModuleException;

    List<ModelAttrAssoc> bindNSAttributeToModel(Long pModelId, Fragment pFragment) throws ModuleException;

    /**
     * Propagate a fragment update
     *
     * @param pFragmentId fragment updated
     * @throws ModuleException if error occurs!
     */
    void updateNSBind(Long pFragmentId) throws ModuleException;

    void unbindNSAttributeToModel(Long pModelId, Long pFragmentId) throws ModuleException;

    Model duplicateModelAttrAssocs(Long pSourceModelId, Model pTargetModel) throws ModuleException;

    Set<ModelAttrAssoc> getComputedAttributes(Long pId);

    Page<AttributeModel> getAttributeModels(List<Long> pModelIds, Pageable pPageable) throws ModuleException;

}
