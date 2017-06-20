/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import java.util.Collection;
import java.util.List;

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

    List<AttributeModel> findByType(AttributeType pType);

    List<AttributeModel> findByTypeAndFragmentName(AttributeType pType, String pFragmentName);

    List<AttributeModel> findByFragmentName(String pFragmentName);

    AttributeModel findByNameAndFragmentName(String pAttributeName, String pFragmentName);

    List<AttributeModel> findByFragmentId(Long pFragmentId);

    Collection<AttributeModel> findByName(String fragmentName);
}
