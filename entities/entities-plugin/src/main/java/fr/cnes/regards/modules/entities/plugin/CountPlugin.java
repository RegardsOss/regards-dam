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
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.IComputedAttributeVisitor;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;

/**
 * This implementation allows to compute the number of {@link DataObject} of a {@link Dataset}
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "CountPlugin", description = "allows to compute the number of data of a Dataset",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss", version = "1.0.0")
public class CountPlugin implements IComputedAttribute<Dataset, Long> {

    @Autowired
    private IEsRepository esRepo;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private IAttributeModelService attModelService;

    @PluginParameter(name = "resultAttributeName", description = "Name of the attribute to compute (ie result attribute).")
    private String attributeToComputeName;

    @PluginParameter(name = "resultAttributeFragmentName",
            description = "Name of the attribute to compute fragment. If the computed attribute belongs to the "
                    + "default fragment, this value can be set to null.")
    private String attributeToComputeFragmentName;

    private AttributeModel attributeToCompute;

    private long count = 0L;

    /**
     * Plugin initialization method
     */
    @PluginInit
    public void init() {
        attributeToCompute = attModelService.findByNameAndFragmentName(attributeToComputeName,
                                                                       attributeToComputeFragmentName);
    }

    @Override
    public Long getResult() {
        return count;
    }

    @Override
    public void compute(Dataset dataset) {
        // create the search
        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(tenantResolver.getTenant(),
                                                                      EntityType.DATA.toString(), DataObject.class);
        esRepo.searchAll(searchKey, object -> count++, dataset.getSubsettingClause());
    }

    @Override
    public AttributeModel getAttributeToCompute() {
        return attributeToCompute;
    }

    @Override
    public AttributeType getSupported() {
        return AttributeType.LONG;
    }

}
