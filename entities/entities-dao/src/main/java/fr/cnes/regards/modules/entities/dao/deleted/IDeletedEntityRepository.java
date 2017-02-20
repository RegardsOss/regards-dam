package fr.cnes.regards.modules.entities.dao.deleted;

import org.springframework.data.repository.CrudRepository;

import fr.cnes.regards.modules.entities.domain.deleted.DeletedEntity;

/**
 * Repository for deleted entities
 */
public interface IDeletedEntityRepository extends CrudRepository<DeletedEntity, Long> {

}
