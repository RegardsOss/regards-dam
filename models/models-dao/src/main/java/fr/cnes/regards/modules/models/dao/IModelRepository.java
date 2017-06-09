/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * {@link Model} repository
 *
 * @author Marc Sordi
 *
 */
@Repository
public interface IModelRepository extends CrudRepository<Model, Long> {

    List<Model> findByType(EntityType pType);

    Model findByName(String pName);

    @Override
    @Modifying
    @Query(value = "TRUNCATE t_model CASCADE", nativeQuery = true)
    void deleteAll();
}
