package fr.cnes.regards.modules.dam.domain.datasources;

import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * Datasource attribute mapping for dynamic fields ie without mapping options.
 * This kind of attribute mapping must have and optionaly a namespace but cannot have mapping options.
 * @author oroussel
 * @author Christophe Mertz
 */
public class DynamicAttributeMapping extends AbstractAttributeMapping {

    public DynamicAttributeMapping() {
        super();
        attributeType = AttributeMappingEnum.DYNAMIC;
    }

    /**
     * Complete constructor
     * @param pName name of attribute in model
     * @param pNameSpace fragment name in model (ie namespace)
     * @param pType attribute type in model
     * @param pMappingDS attribute name in datasource
     */
    public DynamicAttributeMapping(String pName, String pNameSpace, PropertyType pType, String pMappingDS) {
        super(pName, pNameSpace, pType, pMappingDS);
        attributeType = AttributeMappingEnum.DYNAMIC;
    }

    public DynamicAttributeMapping(String pName, PropertyType pType, String pMappingDS) {
        this(pName, null, pType, pMappingDS);
        attributeType = AttributeMappingEnum.DYNAMIC;
    }

}
