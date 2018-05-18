package fr.cnes.regards.modules.entities.dao.deleted;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.entities.domain.deleted.DeletedEntity;

/**
 * Repository for deleted entities
 * @author oroussel
 */
public interface IDeletedEntityRepository extends CrudRepository<DeletedEntity, Long> {
    Optional<DeletedEntity> findOneByIpId(UniformResourceName ipId);
}
