/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.plugin;

import java.time.LocalDateTime;
import java.util.Collection;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(id = "NonUsable", description = "Plugin just for test", author = "Sylvain VISSIERE-GUERINET")
public class NonUsable implements IComputedAttribute<Long, LocalDateTime> {

    @Override
    public LocalDateTime getResult() {
        return null;
    }

    @Override
    public void compute(Collection<Long> pPartialData) {

    }

    @Override
    public AttributeType getSupported() {
        return AttributeType.DATE_ISO8601;
    }

    @Override
    public AttributeModel getAttributeComputed() {
        return null;
    }

}
