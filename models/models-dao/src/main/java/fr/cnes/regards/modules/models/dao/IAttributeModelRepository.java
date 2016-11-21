/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 *
 * {@link AttributeModel} repository
 *
 * @author Marc Sordi
 *
 */
@Repository
public interface IAttributeModelRepository extends CrudRepository<AttributeModel, Long> {

    Iterable<AttributeModel> findByType(AttributeType pType);

    AttributeModel findByNameAndFragmentName(String pAttributeName, String pFragmentName);

    Iterable<AttributeModel> findByFragmentId(Long pFragmentId);
}
