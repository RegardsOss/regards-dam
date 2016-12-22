/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator;

import java.lang.annotation.ElementType;

import javax.validation.Path;
import javax.validation.Path.Node;

import org.hibernate.validator.internal.engine.resolver.DefaultTraversableResolver;

import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;

/**
 * Bypass default resolver for attribute property
 *
 * @author Marc Sordi
 *
 */
public class AttributeTraversableResolver extends DefaultTraversableResolver {

    @Override
    public boolean isReachable(Object pTraversableObject, Node pTraversableProperty, Class<?> pRootBeanType,
            Path pPathToTraversableObject, ElementType pElementType) {
        return super.isReachable(pTraversableObject, pTraversableProperty, pRootBeanType, pPathToTraversableObject,
                                 pElementType);
    }

    @Override
    public boolean isCascadable(Object pTraversableObject, Node pTraversableProperty, Class<?> pRootBeanType,
            Path pPathToTraversableObject, ElementType pElementType) {
        if ((AbstractAttribute.class.isAssignableFrom(pTraversableObject.getClass()))
                && pTraversableProperty.getName().equals("attributes")) {
            return true;
        }
        return super.isCascadable(pTraversableObject, pTraversableProperty, pRootBeanType, pPathToTraversableObject,
                                  pElementType);
    }

}
