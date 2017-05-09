/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.plugin;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;

/**
 * This implementation allows to compute the number of {@link DataObject} of a {@link Dataset}
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "CountElementAttribute", description = "allows to compute the number of data of a Dataset",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss", version = "1.0.0")
public class CountElementAttribute extends AbstractFromDataObjectAttributeComputation<Long> {

    @Autowired
    private IEsRepository esRepo;

    @Autowired
    protected IRuntimeTenantResolver tenantResolver;

    @Autowired
    protected IAttributeModelService attModelService;

    @PluginParameter(name = "attributeToComputeName", description = "Name of the attribute to compute.")
    protected String attributeToComputeName;

    @PluginParameter(name = "attributeToComputeFragmentName",
            description = "Name of the Fragment of the attribute to compute. If the computed attribute belongs to the default fragment, this value can be set to null.")
    protected String attributeToComputeFragmentName;

    /**
     * Plugin initialization method
     */
    @PluginInit
    public void init() {
        initAbstract(esRepo, attModelService, tenantResolver);
        attributeToCompute = attModelService.findByNameAndFragmentName(attributeToComputeName,
                                                                       attributeToComputeFragmentName);
        result = 0L;
    }

    @Override
    public Consumer<DataObject> doCompute() {
        return datum -> result++;
    }

    @Override
    public AttributeType getSupported() {
        return AttributeType.LONG;
    }

}
