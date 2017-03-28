/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.visitor;

import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.models.domain.ICalculationModel;
import fr.cnes.regards.modules.models.domain.ICalculationModelVisitor;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * Visitor handling the logic of creating an AbstractAttribute according to the AttributeModel computed by the
 * ICalculationModel plugin
 *
 * @author Sylvain Vissiere-Guerinet
 */
public class AttributeBuilderVisitor implements ICalculationModelVisitor<AbstractAttribute<?>> {

    @Override
    public <U> AbstractAttribute<?> visit(ICalculationModel<U> pPlugin) {
        AttributeModel attr = pPlugin.getAttributeComputed();
        if (attr.getFragment().isDefaultFragment()) {

            return AttributeBuilder.forType(pPlugin.getSupported(), attr.getName(), pPlugin.getResult());
        } else {
            return AttributeBuilder
                    .buildObject(attr.getFragment().getName(),
                                 AttributeBuilder.forType(pPlugin.getSupported(), attr.getName(), pPlugin.getResult()));
        }
    }

}
