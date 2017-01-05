/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.springframework.data.repository.CrudRepository;

import fr.cnes.regards.modules.models.domain.attributes.AttributeProperty;

/**
 *
 * {@link AttributeProperty} repository
 *
 * @author Marc Sordi
 *
 */
public interface IAttributePropertyRepository extends CrudRepository<AttributeProperty, Long> {

}
