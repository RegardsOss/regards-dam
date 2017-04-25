package fr.cnes.regards.modules.datasources.domain;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Datasource attribute mapping for dynamic fields ie without mapping options.
 * This kind of attribute mapping must have and optionaly a namespace but cannot have mapping options
 * @author oroussel
 */
public class DynamicAttributeMapping extends AbstractAttributeMapping {

    public DynamicAttributeMapping() {
        super();
    }

    /**
     * Complete constructor
     * @param pName name of attribute in model
     * @param pNameSpace fragment name in model (ie namespace)
     * @param pType attribute type in model
     * @param pMappingDS attribute name in datasource
     * @param pTypeDS attribute type in datasource
     */
    public DynamicAttributeMapping(String pName, String pNameSpace, AttributeType pType, String pMappingDS,
            Integer pTypeDS) {
        super(pName, pNameSpace, pType, pMappingDS, pTypeDS, NO_MAPPING_OPTIONS);
    }

    public DynamicAttributeMapping(String pName, String pNameSpace, AttributeType pType, String pMappingDS) {
        this(pName, pNameSpace, pType, pMappingDS, null);
    }

    public DynamicAttributeMapping(String pName, AttributeType pType, String pMappingDS,
            Integer pTypeDS) {
        this(pName, null, pType, pMappingDS, pTypeDS);
    }

    public DynamicAttributeMapping(String pName, AttributeType pType, String pMappingDS) {
        this(pName, pType, pMappingDS, null);
    }
}
