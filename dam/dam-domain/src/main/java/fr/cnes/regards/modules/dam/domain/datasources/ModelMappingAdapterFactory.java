package fr.cnes.regards.modules.dam.domain.datasources;

import fr.cnes.regards.framework.gson.adapters.PolymorphicTypeAdapterFactory;
import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapterFactory;

/**
 * @author Léo Mieulet
 */
@GsonTypeAdapterFactory
public class ModelMappingAdapterFactory extends PolymorphicTypeAdapterFactory<AbstractAttributeMapping> {

    /**
     * Constructor
     */
    public ModelMappingAdapterFactory() {
        super(AbstractAttributeMapping.class, "attributeType", false);
        registerSubtype(DynamicAttributeMapping.class, AttributeMappingEnum.DYNAMIC);
        registerSubtype(StaticAttributeMapping.class, AttributeMappingEnum.STATIC);
    }
}
