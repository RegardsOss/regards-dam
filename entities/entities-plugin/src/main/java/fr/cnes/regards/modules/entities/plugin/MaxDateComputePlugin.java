/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.plugin;

import java.time.OffsetDateTime;
import java.util.Optional;
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
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * This IComputedAttribute implementation allows to compute the maximum of a {@link DateAttribute} according to a
 * collection of {@link DataObject} using the same DateAttribute name
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "MaxDateComputePlugin",
        description = "allows to compute the maximum of a DateAttribute according to a collection of data",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss", version = "1.0.0")
public class MaxDateComputePlugin extends AbstractDataObjectComputePlugin<OffsetDateTime> {

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
    }

    private void getMaxDate(Optional<AbstractAttribute<?>> parameterOpt) {
        if (parameterOpt.isPresent() && (parameterOpt.get() instanceof DateAttribute)) {
            DateAttribute parameter = (DateAttribute) parameterOpt.get();
            OffsetDateTime value = parameter.getValue();
            if (value != null) {
                if (result != null) {
                    result = value.isAfter(result) ? parameter.getValue() : result;
                } else {
                    result = value;
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
        // first extract the properties of the right fragment, then get the max
        return object -> getMaxDate(extractProperty(object));
    }

}
