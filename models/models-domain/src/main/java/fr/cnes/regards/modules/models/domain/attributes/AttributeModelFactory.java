/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes;

import org.springframework.util.Assert;

/**
 *
 * {@link AttributeModel} factory
 *
 * @author msordi
 *
 */
public final class AttributeModelFactory {

    private AttributeModelFactory() {
    }

    public static AttributeModel build(Long pId, String pName, AttributeType pType) {
        Assert.notNull(pId);
        Assert.notNull(pName);
        Assert.notNull(pType);
        final AttributeModel attributeModel = new AttributeModel();
        attributeModel.setId(pId);
        attributeModel.setName(pName);
        attributeModel.setType(pType);
        return attributeModel;
    }
}
