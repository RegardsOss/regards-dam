/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.gson;

import java.util.List;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * Helper class to retrieve attribute list from different microservices with different implementations
 *
 * @author Marc Sordi
 *
 */
@FunctionalInterface
public interface IAttributeHelper {

    List<AttributeModel> getAllAttributes(String tenant);
}
