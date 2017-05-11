/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.plugin;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;

/**
 * This IComputedAttribute implementation allows to compute the minimum of a {@link DateAttribute} according to a
 * collection of {@link DataObject} using the same DateAttribute name
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "MinDateAttribute",
        description = "allows to compute the minimum of a DateAttribute according to a collection of data",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss", version = "1.0.0")
public class MinDateAttribute extends AbstractFromDataObjectAttributeComputation<OffsetDateTime> {

    @Autowired
    private IEsRepository esRepo;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private IAttributeModelService attModelService;

    @PluginParameter(name = "attributeToComputeName", description = "Name of the attribute to compute.")
    private String attributeToComputeName;

    @PluginParameter(name = "attributeToComputeFragmentName",
            description = "Name of the Fragment of the attribute to compute. If the computed attribute belongs to the default fragment, this value can be set to null.")
    private String attributeToComputeFragmentName;

    /**
     * Plugin initialization method
     */
    @PluginInit
    public void init() {
        initAbstract(esRepo, attModelService, tenantResolver);
        attributeToCompute = attModelService.findByNameAndFragmentName(attributeToComputeName,
                                                                       attributeToComputeFragmentName);
    }

    private void getMinDate(Set<AbstractAttribute<?>> pProperties) {
        Optional<AbstractAttribute<?>> candidate = pProperties.stream()
                .filter(p -> p.getName().equals(attributeToCompute.getName())).findFirst();
        if (candidate.isPresent() && (candidate.get() instanceof DateAttribute)) {
            DateAttribute attributeOfInterest = (DateAttribute) candidate.get();
            OffsetDateTime value = attributeOfInterest.getValue();
            if (value != null) {
                if (result != null) {
                    result = attributeOfInterest.getValue().isBefore(result) ? attributeOfInterest.getValue() : result;
                } else {
                    result = attributeOfInterest.getValue();
                }
            }
        }
    }

    @Override
    public AttributeType getSupported() {
        return AttributeType.DATE_ISO8601;
    }

    @Override
    protected Consumer<DataObject> doCompute() {
        return datum -> getMinDate(extractProperties(datum));
    }

}
