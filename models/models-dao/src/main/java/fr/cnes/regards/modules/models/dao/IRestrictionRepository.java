/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;

/**
 * {@link AbstractRestriction} repository
 *
 * @author Marc Sordi
 *
 */
@Repository
public interface IRestrictionRepository extends CrudRepository<AbstractRestriction, Long> {
}
