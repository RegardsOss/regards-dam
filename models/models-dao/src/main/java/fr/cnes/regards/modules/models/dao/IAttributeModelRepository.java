/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 *
 * {@link AttributeModel} repository
 *
 * @author msordi
 *
 */
@Repository
public interface IAttributeModelRepository extends CrudRepository<AttributeModel, Long> {
}
