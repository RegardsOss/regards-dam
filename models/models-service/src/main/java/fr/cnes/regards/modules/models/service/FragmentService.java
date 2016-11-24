/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import fr.cnes.regards.framework.jpa.utils.IterableUtils;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotEmptyException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotIdentifiableException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.dao.IFragmentRepository;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 * Fragment service
 *
 * @author Marc Sordi
 *
 */
@Service
public class FragmentService implements IFragmentService {

    /**
     * {@link Fragment} repository
     */
    private final IFragmentRepository fragmentRepository;

    /**
     * {@link AttributeModel} repository
     */
    private final IAttributeModelRepository attributeModelRepository;

    public FragmentService(IFragmentRepository pFragmentRepository,
            IAttributeModelRepository pAttributeModelRepository) {
        this.fragmentRepository = pFragmentRepository;
        this.attributeModelRepository = pAttributeModelRepository;
    }

    @Override
    public List<Fragment> getFragments() {
        return IterableUtils.toList(fragmentRepository.findAll());
    }

    @Override
    public Fragment addFragment(Fragment pFragment) throws ModuleException {
        final Fragment existing = fragmentRepository.findByName(pFragment.getName());
        if (existing != null) {
            throw new EntityAlreadyExistsException(
                    String.format("Fragment with name \"%s\" already exists!", pFragment.getName()));
        }
        return fragmentRepository.save(pFragment);
    }

    @Override
    public Fragment getFragment(Long pFragmentId) throws ModuleException {
        if (!fragmentRepository.exists(pFragmentId)) {
            throw new EntityNotFoundException(pFragmentId, Fragment.class);
        }
        return fragmentRepository.findOne(pFragmentId);
    }

    @Override
    public Fragment updateFragment(Long pFragmentId, Fragment pFragment) throws ModuleException {
        if (!pFragment.isIdentifiable()) {
            throw new EntityNotIdentifiableException(
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
        if ((attModels != null) || (Iterables.size(attModels) != 0)) {
            throw new EntityNotEmptyException(pFragmentId, Fragment.class);
        }
        if (fragmentRepository.exists(pFragmentId)) {
            fragmentRepository.delete(pFragmentId);
        }
    }
}
