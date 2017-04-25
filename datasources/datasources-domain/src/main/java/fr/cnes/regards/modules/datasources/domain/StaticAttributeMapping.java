package fr.cnes.regards.modules.datasources.domain;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Datasource attribute mapping for static fields ie primary key, label, last update date, raw data, thumbnail or
 * geometry. This kind of attribute mapping doesn't have a name nor a namespace but must have one or more mapping
 * options (mostly only one but nothing prevents to have several).
 * @author oroussel
 */
public class StaticAttributeMapping extends AbstractAttributeMapping {

    public StaticAttributeMapping() {
        super();
    }

    /**
     * Complete constructor
     * @param pType type of attribute in model
     * @param pMappingDS mapping name in datasource
     * @param pTypeDS type of attribute in datasource
     * @param pMappingOptions OR bitwise of Mapping options {@link AbstractAttributeMapping#NO_MAPPING_OPTIONS},
     * @link {@link AbstractAttributeMapping#GEOMETRY}, @link {@link AbstractAttributeMapping#LABEL},
     * @link {@link AbstractAttributeMapping#RAW_DATA}, ...
     */
    public StaticAttributeMapping(AttributeType pType, String pMappingDS,
            Integer pTypeDS, short pMappingOptions) {
        super(null, null, pType, pMappingDS, pTypeDS, pMappingOptions);
    }

    /**
     * Minimal constructor
     * @param pType type of attribute in model
     * @param pMappingDS mapping name in datasource
     * @param pMappingOptions OR bitwise of Mapping options {@link AbstractAttributeMapping#NO_MAPPING_OPTIONS},
     * @link {@link AbstractAttributeMapping#GEOMETRY}, @link {@link AbstractAttributeMapping#LABEL},
     * @link {@link AbstractAttributeMapping#RAW_DATA}, ...
     */
    public StaticAttributeMapping(AttributeType pType, String pMappingDS, short pMappingOptions) {
        this(pType, pMappingDS, null, pMappingOptions);
    }
}
