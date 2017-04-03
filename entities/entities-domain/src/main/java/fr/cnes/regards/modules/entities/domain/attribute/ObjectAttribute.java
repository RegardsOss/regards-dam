/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import java.util.Set;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a model fragment containing a list of attributes
 *
 * @author Marc Sordi
 */
public class ObjectAttribute extends AbstractAttribute<Set<AbstractAttribute<?>>> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return true;
    }

}
