/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

/**
 * Visitor for ICalculationModel plugins
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public interface IComputedAttributeVisitor<T> {

    <U> T visit(IComputedAttribute<U> pPlugin);

}
