/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Plugins of type ICalculationModel are plugins allowing to calculate the value of an {@link AttributeModel} thanks to
 * a {@link ModelAttrAssoc} We are using the design pattern "Visitor" with {@link IComputedAttributeVisitor}.
 *
 * @param <P> Type of entity on which the attribute will be added
 * @param <R> type of the attribute value
 * @author Sylvain Vissiere-Guerinet
 */
@PluginInterface(
        description = "Plugins of type ICalculationModel are plugins allowing to calculate the value of an AttributeModel thanks to a ModelAttrAssoc")
public interface IComputedAttribute<P, R> {

    /**
     * @return the value computed by the implementation.
     */
    R getResult();

    /**
     * Method responsible for computation of the value.
     *
     * @param pTarget object on which the attribute should be added.
     */
    void compute(P pTarget);

    /**
     * @return supported AttributeType.
     */
    AttributeType getSupported();

    default <U> U accept(IComputedAttributeVisitor<U> pVisitor) {
        return pVisitor.visit(this);
    };

    /**
     * @return the attribute computed by this plugin
     */
    AttributeModel getAttributeToCompute();
}
