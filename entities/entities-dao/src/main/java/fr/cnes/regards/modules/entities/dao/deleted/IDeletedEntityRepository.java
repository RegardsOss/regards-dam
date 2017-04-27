package fr.cnes.regards.modules.entities.dao.deleted;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import org.springframework.data.repository.CrudRepository;

import fr.cnes.regards.modules.entities.domain.deleted.DeletedEntity;

import java.util.Optional;

/**
 * Repository for deleted entities
 * @author oroussel
 */
public interface IDeletedEntityRepository extends CrudRepository<DeletedEntity, Long> {
    Optional<DeletedEntity> findOneByIpId(UniformResourceName ipId);
}
