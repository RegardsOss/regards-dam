/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.Collection;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(description = "plugin there just for tests in model-rest", author = "REGARDS Team", contact = "regards@c-s.fr",
        licence = "LGPLv3.0", owner = "CSSI", url = "https://github.com/RegardsOss", id = "TestComputedAttribute",
        version = "1.0.0")
public class TestComputedAttribute implements IComputedAttribute<Long, String> {

    @Override
    public String getResult() {
        return null;
    }

    @Override
    public void compute(Collection<Long> pPartialData) {

    }

    @Override
    public AttributeType getSupported() {
        return AttributeType.STRING;
    }

    @Override
    public AttributeModel getAttributeComputed() {
        return null;
    }

}
