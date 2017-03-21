/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * {@link ModelAttrAssoc} repository
 *
 * @author Marc Sordi
 *
 */
public interface IModelAttrAssocRepository extends CrudRepository<ModelAttrAssoc, Long> {

    List<ModelAttrAssoc> findByModelId(Long pModelId);

    ModelAttrAssoc findByModelIdAndAttribute(Long pModelId, AttributeModel pAttributeModel);
}
