/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import fr.cnes.regards.framework.jpa.utils.IterableUtils;
import fr.cnes.regards.framework.module.rest.exception.ModuleAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.ModuleEntityNotEmptyException;
import fr.cnes.regards.framework.module.rest.exception.ModuleEntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleEntityNotIdentifiableException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.module.rest.exception.ModuleInconsistentEntityIdentifierException;
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
            throw new ModuleAlreadyExistsException(
                    String.format("Fragment with name \"%s\" already exists!", pFragment.getName()));
        }
        return fragmentRepository.save(pFragment);
    }

    @Override
    public Fragment getFragment(Long pFragmentId) throws ModuleException {
        if (!fragmentRepository.exists(pFragmentId)) {
            throw new ModuleEntityNotFoundException(pFragmentId, Fragment.class);
        }
        return fragmentRepository.findOne(pFragmentId);
    }

    @Override
    public Fragment updateFragment(Long pFragmentId, Fragment pFragment) throws ModuleException {
        if (!pFragment.isIdentifiable()) {
            throw new ModuleEntityNotIdentifiableException(
                    String.format("Unknown identifier for fragment \"%s\"", pFragment.getName()));
        }
        if (!pFragmentId.equals(pFragment.getId())) {
            throw new ModuleInconsistentEntityIdentifierException(pFragmentId, pFragment.getId(), Fragment.class);
        }
        if (!fragmentRepository.exists(pFragmentId)) {
            throw new ModuleEntityNotFoundException(pFragmentId, Fragment.class);
        }
        return fragmentRepository.save(pFragment);
    }

    @Override
    public void deleteFragment(Long pFragmentId) throws ModuleException {
        // Check if fragment is empty
        final Iterable<AttributeModel> attModels = attributeModelRepository.findByFragmentId(pFragmentId);
        if ((attModels != null) || (Iterables.size(attModels) != 0)) {
            throw new ModuleEntityNotEmptyException(pFragmentId, Fragment.class);
        }
        if (fragmentRepository.exists(pFragmentId)) {
            fragmentRepository.delete(pFragmentId);
        }
    }

}
