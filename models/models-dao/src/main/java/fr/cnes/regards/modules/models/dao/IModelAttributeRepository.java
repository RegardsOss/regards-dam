/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * {@link ModelAttribute} repository
 *
 * @author Marc Sordi
 *
 */
public interface IModelAttributeRepository extends CrudRepository<ModelAttribute, Long> {

    List<ModelAttribute> findByModelId(Long pModelId);

    ModelAttribute findByModelIdAndAttribute(Long pModelId, AttributeModel pAttributeModel);
}
