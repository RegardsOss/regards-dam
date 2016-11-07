/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 *
 * {@link Fragment} repository
 *
 * @author Marc Sordi
 *
 */
@Repository
public interface IFragmentRepository extends CrudRepository<Fragment, Long> {

    Fragment findByName(String pName);
}
