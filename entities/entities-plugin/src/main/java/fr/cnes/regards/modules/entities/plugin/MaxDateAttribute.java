/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.plugin;

import java.time.LocalDateTime;
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
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;

/**
 * This IComputedAttribute implementation allows to compute the maximum of a {@link DateAttribute} according to a
 * collection of {@link DataObject} using the same DateAttribute name
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "MaxDateAttribute", author = "Sylvain VISSIERE-GUERINET",
        description = "allows to compute the maximum of a DateAttribute according to a collection of data")
public class MaxDateAttribute implements IComputedAttribute<LocalDateTime> {

    @Autowired
    private IAttributeModelService attModelService;

    @PluginParameter(name = "attributeToComputeName", description = "Name of the attribute to compute.")
    private String attributeToComputeName;

    @PluginParameter(name = "attributeToComputeFragmentName",
            description = "Name of the Fragment of the attribute to compute. If the computed attribute belongs to the default fragment, this value can be set to null.")
    private String attributeToComputeFragmentName;

    private AttributeModel attributeToCompute;

    private LocalDateTime result;

    @PluginInit
    public void init() {
        attributeToCompute = attModelService.findByNameAndFragmentName(attributeToComputeName,
                                                                       attributeToComputeFragmentName);
    }

    @Override
    public LocalDateTime getResult() {
        return result;
    }

    /**
     * This implementation only compute on a collection of {@link DataObject}
     */
    @Override
    public void compute(Collection<?> pPartialData) {
        if (pPartialData.getClass().getTypeName().contains("<" + DataObject.class.getCanonicalName() + ">")) {
            @SuppressWarnings("unchecked")
            Collection<DataObject> data = (Collection<DataObject>) pPartialData;
            for (DataObject datum : data) {
                if (attributeToCompute.getFragment().isDefaultFragment()) {
                    // the attribute is in the default fragment so it has at the root level of properties
                    getMaxDate(datum.getProperties());
                } else {
                    // the attribute is in a fragment so we have to be get the right fragment(ObjectAttribute) before we
                    // can access the attribute
                    Set<AbstractAttribute<?>> candidates = datum.getProperties().stream()
                            .filter(p -> (p instanceof ObjectAttribute)
                                    && p.getName().equals(attributeToCompute.getFragment().getName()))
                            .flatMap(fragment -> ((ObjectAttribute) fragment).getValue().stream())
                            .collect(Collectors.toSet());
                    getMaxDate(candidates);
                }
            }
        }
    }

    private void getMaxDate(Set<AbstractAttribute<?>> pProperties) {
        Optional<AbstractAttribute<?>> candidate = pProperties.stream()
                .filter(p -> p.getName().equals(attributeToCompute.getName())).findFirst();
        if (candidate.isPresent() && (candidate.get() instanceof DateAttribute)) {
            DateAttribute attributeOfInterest = (DateAttribute) candidate.get();
            result = attributeOfInterest.getValue().isAfter(result) ? attributeOfInterest.getValue() : result;
        }
    }

    @Override
    public AttributeType getSupported() {
        return AttributeType.DATE_ISO8601;
    }

    @Override
    public AttributeModel getAttributeComputed() {
        return attributeToCompute;
    }
}
