/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.plugin;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;

/**
 * This implementation allows to compute the number of {@link DataObject} of a {@link Dataset}
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "CountElementAttribute", description = "allows to compute the number of data of a Dataset",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "http://www.c-s.fr/", version = "1.0.0")
public class CountElementAttribute implements IComputedAttribute<DataObject, Long> {

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
        Collection<DataObject> data = pPartialData;
        result += data.size();
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
