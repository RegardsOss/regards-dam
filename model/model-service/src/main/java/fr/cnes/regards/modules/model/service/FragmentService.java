/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.model.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import fr.cnes.regards.modules.model.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.model.dao.IFragmentRepository;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.Fragment;
import fr.cnes.regards.modules.model.domain.event.FragmentDeletedEvent;
import fr.cnes.regards.modules.model.service.xml.XmlExportHelper;
import fr.cnes.regards.modules.model.service.xml.XmlImportHelper;

/**
 * Fragment service
 * @author Marc Sordi
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

    public FragmentService(IFragmentRepository fragmentRepository, IAttributeModelRepository attributeModelRepository,
            IAttributeModelService attributeModelService, IPublisher publisher) {
        this.fragmentRepository = fragmentRepository;
        this.attributeModelRepository = attributeModelRepository;
        this.attributeModelService = attributeModelService;
        this.publisher = publisher;
    }

    @Override
    public List<Fragment> getFragments() {
        Iterable<Fragment> fragments = fragmentRepository.findAll();
        return ((fragments != null) && fragments.iterator().hasNext()) ? ImmutableList.copyOf(fragments)
                : Collections.emptyList();
    }

    @Override
    public Fragment addFragment(Fragment pFragment) throws ModuleException {
        Fragment existing = fragmentRepository.findByName(pFragment.getName());
        if (existing != null) {
            throw new EntityAlreadyExistsException(
                    String.format("Fragment with name \"%s\" already exists!", pFragment.getName()));
        }
        if (!attributeModelService.isFragmentCreatable(pFragment.getName())) {
            throw new EntityAlreadyExistsException(String
                    .format("Fragment with name \"%s\" cannot be created because an attribute with the same name already exists!",
                            pFragment.getName()));
        }
        return fragmentRepository.save(pFragment);
    }

    @Override
    public Fragment getFragment(Long pFragmentId) throws ModuleException {
        Optional<Fragment> fragmentOpt = fragmentRepository.findById(pFragmentId);
        if (!fragmentOpt.isPresent()) {
            throw new EntityNotFoundException(pFragmentId, Fragment.class);
        }
        return fragmentOpt.get();
    }

    @Override
    public Fragment updateFragment(Long fragmentId, Fragment fragment) throws ModuleException {
        if (!fragment.isIdentifiable()) {
            throw new EntityNotFoundException(
                    String.format("Unknown identifier for fragment \"%s\"", fragment.getName()));
        }
        if (!fragmentId.equals(fragment.getId())) {
            throw new EntityInconsistentIdentifierException(fragmentId, fragment.getId(), Fragment.class);
        }
        if (!fragmentRepository.existsById(fragmentId)) {
            throw new EntityNotFoundException(fragmentId, Fragment.class);
        }
        return fragmentRepository.save(fragment);
    }

    @Override
    public void deleteFragment(Long fragmentId) throws ModuleException {
        // Check if fragment is empty
        Iterable<AttributeModel> attModels = attributeModelRepository.findByFragmentId(fragmentId);
        if ((attModels != null) && !Iterables.isEmpty(attModels)) {
            throw new EntityNotEmptyException(fragmentId, Fragment.class);
        }
        Optional<Fragment> fragmentOpt = fragmentRepository.findById(fragmentId);
        if (fragmentOpt.isPresent()) {
            fragmentRepository.delete(fragmentOpt.get());
            publisher.publish(new FragmentDeletedEvent(fragmentOpt.get().getName()));
        }
    }

    @Override
    public void exportFragment(Long fragmentId, OutputStream os) throws ModuleException {
        // Get fragment
        Fragment fragment = getFragment(fragmentId);
        // Get all related attributes
        List<AttributeModel> attModels = attributeModelRepository.findByFragmentId(fragmentId);
        // Export fragment to output stream
        XmlExportHelper.exportFragment(os, fragment, attModels);
    }

    @Override
    public Fragment importFragment(InputStream is) throws ModuleException {
        // Import fragment from input stream
        List<AttributeModel> attModels = XmlImportHelper.importFragment(is);
        // Insert attributes
        attributeModelService.addAllAttributes(attModels);
        return attModels.get(0).getFragment();
    }
}
