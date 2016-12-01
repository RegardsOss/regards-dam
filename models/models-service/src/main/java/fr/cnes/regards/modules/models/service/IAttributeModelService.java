/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import javax.transaction.Transactional;

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

    /**
     * Add an attribute in a {@link Transactional} context
     *
     * @param pAttributeModel
     *            {@link AttributeModel} to add
     * @return {@link AttributeModel}
     * @throws ModuleException
     *             if error occurs!
     */
    AttributeModel addAttribute(AttributeModel pAttributeModel) throws ModuleException;

    /**
     * Add a list of attributes in a {@link Transactional} context
     *
     * @param pAttributeModels
     *            list of {@link AttributeModel} to add
     * @return list of {@link AttributeModel}
     * @throws ModuleException
     *             if error occurs!
     */
    Iterable<AttributeModel> addAllAttributes(Iterable<AttributeModel> pAttributeModels) throws ModuleException;

    /**
     * Manage {@link AttributeModel} creation out of a {@link Transactional} context. This method is used by
     * {@link IAttributeModelService#addAttribute(AttributeModel)} and
     * {@link IAttributeModelService#addAllAttributes(Iterable)}.
     *
     * @param pAttributeModel
     *            {@link AttributeModel} to create
     * @return {@link AttributeModel}
     * @throws ModuleException
     *             if error occurs!
     */
    AttributeModel createAttribute(AttributeModel pAttributeModel) throws ModuleException;

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
