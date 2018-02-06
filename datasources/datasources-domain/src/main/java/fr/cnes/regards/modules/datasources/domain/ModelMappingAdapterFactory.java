package fr.cnes.regards.modules.datasources.domain;

import fr.cnes.regards.framework.gson.adapters.PolymorphicTypeAdapterFactory;
import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapterFactory;

/**
 * @author Léo Mieulet
 */
@GsonTypeAdapterFactory
public class ModelMappingAdapterFactory extends PolymorphicTypeAdapterFactory<AbstractAttributeMapping> {

    public static final String TYPE_DYNAMIC = "dynamic";

    public static final String TYPE_STATIC = "static";

    /**
     * Constructor
     */
    public ModelMappingAdapterFactory() {
        super(AbstractAttributeMapping.class, "attributeType", false);
        registerSubtype(DynamicAttributeMapping.class, AttributeMappingEnum.DYNAMIC);
        registerSubtype(StaticAttributeMapping.class, AttributeMappingEnum.STATIC);
    }
}
