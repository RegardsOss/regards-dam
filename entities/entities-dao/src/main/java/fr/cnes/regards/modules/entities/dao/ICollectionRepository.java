/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.entities.domain.Collection;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Repository
public interface ICollectionRepository extends IAbstractEntityRepository<Collection> {

    List<Collection> findByGroups(String group);

    @Query("from Collection col left join fetch col.descriptionFile where col.id=:id")
    Collection findOneWithDescriptionFile(@Param("id") Long collectionId);
}
