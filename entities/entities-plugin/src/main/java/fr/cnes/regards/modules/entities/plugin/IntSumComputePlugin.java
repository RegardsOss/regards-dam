/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.plugin;

import java.util.Optional;
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
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * This Implementation of IComputedAttribute allows to compute the sum of {@link IntegerAttribute} according to a
 * collection of {@link DataObject} using the same IntegerAttribute name
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "IntSumComputePlugin", version = "1.0.0",
        description = "allows to compute the sum of IntegerAttribute according to a collection of data using the same IntegerAttribute name",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class IntSumComputePlugin extends AbstractDataObjectComputePlugin<Integer> {

    @Autowired
    private IEsRepository esRepo;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private IAttributeModelRepository attModelRepos;

    @PluginParameter(name = "resultAttributeName",
            description = "Name of the attribute to compute (ie result attribute).")
    private String attributeToComputeName;

    @PluginParameter(name = "resultAttributeFragmentName",
            description = "Name of the attribute to compute fragment. If the computed attribute belongs to the default fragment, this value can be set to null.",
            optional = true)
    private String attributeToComputeFragmentName;

    @PluginParameter(name = "parameterAttributeName",
            description = "Name of the parameter attribute used to compute result attribute.")
    private String parameterAttributeName;

    @PluginParameter(name = "parameterAttributeFragmentName",
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
        super.result = 0;
    }

    private void doSum(Optional<AbstractAttribute<?>> propertyOpt) {
        if (propertyOpt.isPresent() && (propertyOpt.get() instanceof IntegerAttribute)) {
            IntegerAttribute property = (IntegerAttribute) propertyOpt.get();
            Integer value = property.getValue();
            if (value != null) {
                super.result += value;
            }
        }
    }

    @Override
    public AttributeType getSupported() {
        return AttributeType.INTEGER;
    }

    @Override
    protected Consumer<DataObject> doCompute() {
        super.result = 0;
        return object -> doSum(extractProperty(object));
    }

}
