/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.exception.UnsupportedRestrictionException;

/**
 * Attribute management service
 *
 * @author msordi
 *
 */
public interface IAttributeModelService {

    List<AttributeModel> getAttributes(AttributeType pType, String pFragmentName, Set<Long> modelIds);

    /**
     * Add an attribute in a {@link Transactional} context
     *
     * @param pAttributeModel
     *            {@link AttributeModel} to add
     * @param duringImport
     * @return {@link AttributeModel}
     * @throws ModuleException
     *             if error occurs!
     */
    AttributeModel addAttribute(AttributeModel pAttributeModel, boolean duringImport) throws ModuleException;

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
     * {@link IAttributeModelService#addAttribute(AttributeModel, boolean)} and
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

    /**
     * Find attributes by fragment id
     * @param pFragmentId
     * @return attribute which fragment id is the given one
     */
    List<AttributeModel> findByFragmentId(Long pFragmentId);

    /**
     * Find attributes by fragment name
     * @param pFragmentName
     * @return attribute which fragment name is the given one
     */
    List<AttributeModel> findByFragmentName(String pFragmentName);

    void checkRestrictionSupport(AttributeModel pAttributeModel) throws UnsupportedRestrictionException;

    AttributeModel findByNameAndFragmentName(String pAttributeName, String pFragmentName);

    /**
     * Determines whether a fragment can be created without conflicting name with any existing attribute
     * @param fragmentName
     * @return
     */
    boolean isFragmentCreatable(String fragmentName);
}
