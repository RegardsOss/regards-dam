/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

import java.util.Collection;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Plugins of type ICalculationModel are plugins allowing to calculate the value of an {@link AttributeModel} thanks to
 * a {@link ModelAttrAssoc} We are using the design pattern "Visitor" with {@link IComputedAttributeVisitor}. For memory
 * issue, it is most likely that the system cannot easly handle the whole data needed to compute the attribute value. We
 * strongly suggest to use an accumulator variable into implementations that is returned by
 * {@link IComputedAttribute#getResult()}
 *
 * @param <R> type of the attribute value
 * @author Sylvain Vissiere-Guerinet
 */
@PluginInterface(
        description = "Plugins of type ICalculationModel are plugins allowing to calculate the value of an AttributeModel thanks to a ModelAttrAssoc")
public interface IComputedAttribute<R> {

    /**
     * @return the value computed by the implementation.
     */
    R getResult();

    /**
     * Method responsible for ingremental computation of the value. It is of the responsability of the method to check
     * the type of the element of the collection
     *
     * @param pPartialData part of the total data needed to compute the value
     */
    void compute(Collection<?> pPartialData);

    /**
     * @return supported AttributeType.
     */
    AttributeType getSupported();

    <U> U accept(IComputedAttributeVisitor<U> pVisitor);

    /**
     * @return the attribute computed by this plugin
     */
    AttributeModel getAttributeComputed();
}
