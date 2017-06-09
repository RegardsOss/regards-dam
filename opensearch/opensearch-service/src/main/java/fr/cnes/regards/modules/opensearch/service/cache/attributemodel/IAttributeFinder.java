/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.cache.attributemodel;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;

/**
 * Retrieve (fake) static or (real) dynamic attributes based on open search parameter name.
 *
 * @author Marc Sordi
 *
 */
@FunctionalInterface
public interface IAttributeFinder {

    /**
    * Return the {@link AttributeModel} related to the passed name
    * @param name open search parameter name
    * @return the {@link AttributeModel}
    * @throws OpenSearchUnknownParameter if parameter name cannot be mapped to an attribute
    */
    AttributeModel findByName(String name) throws OpenSearchUnknownParameter;
}
