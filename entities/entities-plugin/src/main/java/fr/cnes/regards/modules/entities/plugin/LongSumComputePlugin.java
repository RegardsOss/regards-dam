/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.LongAttribute;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * This Implementation of IComputedAttribute allows to compute the sum of {@link LongAttribute} according to a
 * collection of {@link DataObject} using the same LongAttribute name
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "LongSumComputePlugin", version = "1.0.0",
        description = "allows to compute the sum of LongAttribute according to a collection of data using the same LongAttribute name",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class LongSumComputePlugin extends AbstractDataObjectComputePlugin<Long> {

    @Autowired
    private IEsRepository esRepo;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private IAttributeModelRepository attModelRepos;

    @PluginParameter(name = RESULT_ATTRIBUTE_NAME,
            description = "Name of the attribute to compute (ie result attribute).")
    private String attributeToComputeName;

    @PluginParameter(name = RESULT_FRAGMENT_NAME,
            description = "Name of the attribute to compute fragment. If the computed attribute belongs to the default fragment, this value can be set to null.",
            optional = true)
    private String attributeToComputeFragmentName;

    @PluginParameter(name = PARAMETER_ATTRIBUTE_NAME,
            description = "Name of the parameter attribute used to compute result attribute.")
    private String parameterAttributeName;

    @PluginParameter(name = PARAMETER_FRAGMENT_NAME,
            description = "Name of the parameter attribute fragment. If the parameter attribute belongs to the default fragment, this value can be set to null.",
            optional = true)
    private String parameterAttributeFragmentName;

    /**
     * Plugin initialization method
     */
    @PluginInit
    public void init() {
        super.initAbstract(esRepo, attModelRepos, tenantResolver);
        super.init(attributeToComputeName, attributeToComputeFragmentName, parameterAttributeName,
                   parameterAttributeFragmentName);
        super.result = 0L;
    }

    private void doSum(Optional<AbstractAttribute<?>> propertyOpt) {
        if (propertyOpt.isPresent()) {
            Long value = ((Number) propertyOpt.get().getValue()).longValue();
            if (value != null) {
                super.result += value;
            }
        }
    }

    /**
     * @param dataset dataset on which the attribute, once computed, will be added. This allows us to know which
     * DataObject should be used.
     */
    @Override
    public void compute(Dataset dataset) {
        result = null;
        // create the search
        SimpleSearchKey<DataObject> searchKey = new SimpleSearchKey<>(tenantResolver.getTenant(),
                                                                      EntityType.DATA.toString(), DataObject.class);
        Double doubleResult = esRepo.sum(searchKey, dataset.getSubsettingClause(), parameterAttribute.getJsonPath());
        result = doubleResult.longValue();
        LOG.debug("Attribute {} computed for Dataset {}. Result: {}", parameterAttribute.getJsonPath(),
                  dataset.getIpId().toString(), result);
    }

    @Override
    public AttributeType getSupported() {
        return AttributeType.LONG;
    }

    @Override
    protected Consumer<DataObject> doCompute() {
        super.result = 0L;
        return object -> doSum(extractProperty(object));
    }

}
