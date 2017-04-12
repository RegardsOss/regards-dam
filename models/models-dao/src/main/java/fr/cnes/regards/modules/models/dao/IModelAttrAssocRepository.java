/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * {@link ModelAttrAssoc} repository
 *
 * @author Marc Sordi
 */
public interface IModelAttrAssocRepository extends JpaRepository<ModelAttrAssoc, Long> {

    List<ModelAttrAssoc> findByModelId(Long pModelId);

    ModelAttrAssoc findByModelIdAndAttribute(Long pModelId, AttributeModel pAttributeModel);

    @Query("SELECT assoc.attribute FROM ModelAttrAssoc assoc WHERE assoc.model.id IN :modelIds")
    Page<AttributeModel> findAllAttributeByModelIdIn(@Param("modelIds") List<Long> pModelIds, Pageable pPageable);
}
