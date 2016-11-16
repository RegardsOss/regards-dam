/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelType;

/**
 *
 * {@link Model} repository
 * 
 * @author Marc Sordi
 *
 */
@Repository
public interface IModelRepository extends CrudRepository<Model, Long> {

    Iterable<Model> findByType(ModelType pType);

    Model findByName(String pName);
}
