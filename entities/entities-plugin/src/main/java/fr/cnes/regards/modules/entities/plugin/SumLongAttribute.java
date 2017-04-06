/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.plugin;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.LongAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;

/**
 * This Implementation of IComputedAttribute allows to compute the sum of {@link LongAttribute} according to a
 * collection of {@link DataObject} using the same LongAttribute name
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "SumLongAttribute", version = "1.0.0",
        description = "allows to compute the sum of LongAttribute according to a collection of data using the same LongAttribute name",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class SumLongAttribute implements IComputedAttribute<DataObject, Long> {

    @Autowired
    private IAttributeModelService attModelService;

    @PluginParameter(name = "attributeToComputeName", description = "Name of the attribute to compute.")
    private String attributeToComputeName;

    @PluginParameter(name = "attributeToComputeFragmentName",
            description = "Name of the Fragment of the attribute to compute. If the computed attribute belongs to the default fragment, this value can be set to null.")
    private String attributeToComputeFragmentName;

    private AttributeModel attributeToCompute;

    private Long result = 0L;

    @PluginInit
    public void init() {
        attributeToCompute = attModelService.findByNameAndFragmentName(attributeToComputeName,
                                                                       attributeToComputeFragmentName);
    }

    @Override
    public Long getResult() {
        return result;
    }

    @Override
    public void compute(Collection<DataObject> pPartialData) {
        for (DataObject datum : pPartialData) {
            if (attributeToCompute.getFragment().isDefaultFragment()) {
                // the attribute is in the default fragment so it has at the root level of properties
                doSum(datum.getProperties());
            } else {
                // the attribute is in a fragment so we have to be get the right fragment(ObjectAttribute) before we
                // can access the attribute
                Set<AbstractAttribute<?>> candidates = datum.getProperties().stream()
                        .filter(p -> (p instanceof ObjectAttribute)
                                && p.getName().equals(attributeToCompute.getFragment().getName()))
                        .flatMap(fragment -> ((ObjectAttribute) fragment).getValue().stream())
                        .collect(Collectors.toSet());
                doSum(candidates);
            }
        }
    }

    private void doSum(Set<AbstractAttribute<?>> pProperties) {
        Optional<AbstractAttribute<?>> candidate = pProperties.stream()
                .filter(p -> p.getName().equals(attributeToCompute.getName())).findFirst();
        if (candidate.isPresent() && (candidate.get() instanceof LongAttribute)) {
            LongAttribute attributeOfInterest = (LongAttribute) candidate.get();
            result += attributeOfInterest.getValue();
        }
    }

    @Override
    public AttributeType getSupported() {
        return AttributeType.LONG;
    }

    @Override
    public AttributeModel getAttributeComputed() {
        return attributeToCompute;
    }

}
