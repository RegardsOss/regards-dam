package fr.cnes.regards.modules.datasources.domain;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Datasource attribute mapping for static fields ie primary key, label, last update date, raw data, thumbnail or
 * geometry. This kind of attribute mapping doesn't have a namespace.
 * @author oroussel
 * @author Christophe Mertz
 */
public class StaticAttributeMapping extends AbstractAttributeMapping {

    public StaticAttributeMapping() {
        super();
    }

    /**
     * Minimal constructor
     * @param name the attribute name in model
     * @param mappingDS mapping name in datasource
     */
    public StaticAttributeMapping(String name, String mappingDS) {
        super(name, null, null, mappingDS);
    }

    /**
     * Complete constructor
     * @param name the attribute name in model
     * @param type the attribute type in model
     * @param mappingDS mapping name in datasource
     */
    public StaticAttributeMapping(String name, AttributeType type, String mappingDS) {
        super(name, null, type, mappingDS);
    }

}
