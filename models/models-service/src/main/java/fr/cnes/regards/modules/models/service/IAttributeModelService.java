/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Attribute management service
 *
 * @author msordi
 *
 */
public interface IAttributeModelService {

    List<AttributeModel> getAttributes(AttributeType pType, String pFragmentName);

    AttributeModel addAttribute(AttributeModel pAttributeModel) throws ModuleException;

    Iterable<AttributeModel> addAllAttributes(Iterable<AttributeModel> pAttributeModels) throws ModuleException;

    AttributeModel getAttribute(Long pAttributeId) throws ModuleException;

    AttributeModel updateAttribute(Long pAttributeId, AttributeModel pAttributeModel) throws ModuleException;

    void deleteAttribute(Long pAttributeId);

    /**
     * Check if attribute is linked to a particular fragment (not default one)
     *
     * @param pAttributeId
     *            attribute to check
     * @return true if attribute linked to a particular fragment
     * @throws ModuleException
     *             if fragment does not exist
     */
    boolean isFragmentAttribute(Long pAttributeId) throws ModuleException;

    List<AttributeModel> findByFragmentId(Long pFragmentId) throws ModuleException;
}
