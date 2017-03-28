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
public interface ICalculationModelVisitor<T> {

    <U> T visit(ICalculationModel<U> pPlugin);

}
