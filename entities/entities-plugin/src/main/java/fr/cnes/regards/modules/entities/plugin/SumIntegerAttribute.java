/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.plugin;

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
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;

/**
 * This Implementation of IComputedAttribute allows to compute the sum of {@link IntegerAttribute} according to a
 * collection of {@link DataObject} using the same IntegerAttribute name
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "SumIntegerAttribute", version = "1.0.0",
        description = "allows to compute the sum of IntegerAttribute according to a collection of data using the same IntegerAttribute name",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class SumIntegerAttribute extends AbstractFromDataObjectAttributeComputation<Integer> {

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

    @PluginInit
    public void init() {
        initAbstract(esRepo, attModelService, tenantResolver);
        attributeToCompute = attModelService.findByNameAndFragmentName(attributeToComputeName,
                                                                       attributeToComputeFragmentName);
    }

    private void doSum(Set<AbstractAttribute<?>> pProperties) {
        Optional<AbstractAttribute<?>> candidate = pProperties.stream()
                .filter(p -> p.getName().equals(attributeToCompute.getName())).findFirst();
        if (candidate.isPresent() && (candidate.get() instanceof IntegerAttribute)) {
            IntegerAttribute attributeOfInterest = (IntegerAttribute) candidate.get();
            result += attributeOfInterest.getValue();
        }
    }

    @Override
    public AttributeType getSupported() {
        return AttributeType.INTEGER;
    }

    @Override
    protected Consumer<DataObject> doCompute() {
        return datum -> doSum(extractProperties(datum));
    }

}
