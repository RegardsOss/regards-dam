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
package fr.cnes.regards.modules.dam.service.models;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotEmptyException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.dam.dao.models.IAttributeModelRepository;
import fr.cnes.regards.modules.dam.dao.models.IFragmentRepository;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.Fragment;
import fr.cnes.regards.modules.dam.domain.models.event.FragmentDeletedEvent;
import fr.cnes.regards.modules.dam.service.models.xml.XmlExportHelper;
import fr.cnes.regards.modules.dam.service.models.xml.XmlImportHelper;

/**
 * Fragment service
 *
 * @author Marc Sordi
 *
 */
@Service
@MultitenantTransactional
public class FragmentService implements IFragmentService {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentService.class);

    /**
     * {@link Fragment} repository
     */
    private final IFragmentRepository fragmentRepository;

    /**
     * {@link AttributeModel} repository
     */
    private final IAttributeModelRepository attributeModelRepository;

    /**
     * {@link AttributeModelService}
     */
    private final IAttributeModelService attributeModelService;

    /**
     * {@link IPublisher} instance
     */
    private final IPublisher publisher;

    public FragmentService(IFragmentRepository pFragmentRepository, IAttributeModelRepository pAttributeModelRepository,
            IAttributeModelService pAttributeModelService, IPublisher publisher) {
        this.fragmentRepository = pFragmentRepository;
        this.attributeModelRepository = pAttributeModelRepository;
        this.attributeModelService = pAttributeModelService;
        this.publisher = publisher;
    }

    @Override
    public List<Fragment> getFragments() {
        Iterable<Fragment> fragments = fragmentRepository.findAll();
        return (fragments != null) ? ImmutableList.copyOf(fragments) : Collections.emptyList();
    }

    @Override
    public Fragment addFragment(Fragment pFragment) throws ModuleException {
        final Fragment existing = fragmentRepository.findByName(pFragment.getName());
        if (existing != null) {
            throw new EntityAlreadyExistsException(
                    String.format("Fragment with name \"%s\" already exists!", pFragment.getName()));
        }
        if (!attributeModelService.isFragmentCreatable(pFragment.getName())) {
            throw new EntityAlreadyExistsException(
                    String.format("Fragment with name \"%s\" cannot be created because an attribute with the same name already exists!",
                                  pFragment.getName()));
        }
        return fragmentRepository.save(pFragment);
    }

    @Override
    public Fragment getFragment(Long pFragmentId) throws ModuleException {
        final Fragment fragment = fragmentRepository.findOne(pFragmentId);
        if (fragment == null) {
            throw new EntityNotFoundException(pFragmentId, Fragment.class);
        }
        return fragment;
    }

    @Override
    public Fragment updateFragment(Long pFragmentId, Fragment pFragment) throws ModuleException {
        if (!pFragment.isIdentifiable()) {
            throw new EntityNotFoundException(
                    String.format("Unknown identifier for fragment \"%s\"", pFragment.getName()));
        }
        if (!pFragmentId.equals(pFragment.getId())) {
            throw new EntityInconsistentIdentifierException(pFragmentId, pFragment.getId(), Fragment.class);
        }
        if (!fragmentRepository.exists(pFragmentId)) {
            throw new EntityNotFoundException(pFragmentId, Fragment.class);
        }
        return fragmentRepository.save(pFragment);
    }

    @Override
    public void deleteFragment(Long pFragmentId) throws ModuleException {
        // Check if fragment is empty
        final Iterable<AttributeModel> attModels = attributeModelRepository.findByFragmentId(pFragmentId);
        if ((attModels != null) && !Iterables.isEmpty(attModels)) {
            throw new EntityNotEmptyException(pFragmentId, Fragment.class);
        }
        if (fragmentRepository.exists(pFragmentId)) {
            Fragment toDelete = fragmentRepository.findOne(pFragmentId);
            fragmentRepository.delete(toDelete);
            publisher.publish(new FragmentDeletedEvent(toDelete.getName()));
        }
    }

    @Override
    public void exportFragment(Long pFragmentId, OutputStream pOutputStream) throws ModuleException {
        // Get fragment
        final Fragment fragment = getFragment(pFragmentId);
        // Get all related attributes
        final List<AttributeModel> attModels = attributeModelRepository.findByFragmentId(pFragmentId);
        // Export fragment to output stream
        XmlExportHelper.exportFragment(pOutputStream, fragment, attModels);
    }

    @Override
    public Fragment importFragment(InputStream pInputStream) throws ModuleException {
        // Import fragment from input stream
        final List<AttributeModel> attModels = XmlImportHelper.importFragment(pInputStream);
        // Insert attributes
        attributeModelService.addAllAttributes(attModels);
        return attModels.get(0).getFragment();
    }
}