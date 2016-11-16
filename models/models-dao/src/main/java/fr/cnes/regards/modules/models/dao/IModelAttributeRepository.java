/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.springframework.data.repository.CrudRepository;

import fr.cnes.regards.modules.models.domain.ModelAttribute;

/**
 * {@link ModelAttribute} repository
 *
 * @author Marc Sordi
 *
 */
public interface IModelAttributeRepository extends CrudRepository<ModelAttribute, Long> {

    Iterable<ModelAttribute> findByModelId(Long pModelId);
}
