package fr.cnes.regards.modules.dam.dao.entities;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import fr.cnes.regards.framework.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.domain.entities.DeletedEntity;

/**
 * Repository for deleted entities
 * @author oroussel
 */
public interface IDeletedEntityRepository extends CrudRepository<DeletedEntity, Long> {

    Optional<DeletedEntity> findOneByIpId(UniformResourceName ipId);
}
