/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.datasources.domain;

import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * This class is used to map a data source attribute to an attribute of a {@link Model}
 *
 * @author Christophe Mertz
 * @author oroussel
 */
public abstract class AbstractAttributeMapping {

    /**
     * Constant used for the primary key attribute
     */
    public static final String PRIMARY_KEY = "sipId";

    /**
     * Constant used for the last update attribute
     */
    public static final String LAST_UPDATE = "lastUpdate";

    /**
     * Constant used for the label attribute
     */
    public static final String LABEL = "label";

    /**
     * Constant used for the raw data attribute
     */
    public static final String RAW_DATA = "download";

    /**
     * Constant used for the thumbnail attribute 
     */
    public static final String THUMBNAIL = "thumbnail";

    /**
     * Constant used for the geometry attribute
     */
    public static final String GEOMETRY = "geometry";

    /**
     * The attribute name in the model
     */
    protected String name;

    /**
     * The attribute type in the model
     */
    protected AttributeType type;

    /**
     * The attribute namespace in the model
     */
    protected String nameSpace = null;

    /**
     * The attribute name in the data source
     */
    protected String nameDS;

    protected AttributeMappingEnum attributeType;

    protected AbstractAttributeMapping() {
    }

    /**
     * Constructor with all attributes
     * @param name the attribute name in the model
     * @param nameSpace the attribute name space in the model
     * @param type the attribute type in the model @see {@link AttributeType}
     * @param mappingDS The attribute name in the data source
     */
    protected AbstractAttributeMapping(String name, String nameSpace, AttributeType type, String mappingDS) {
        this.name = name;
        this.nameSpace = nameSpace;
        this.nameDS = mappingDS;
        if (type == null && isMappedToStaticProperty()) {
            this.type = getStaticAttributeType(name);
        } else {
            this.type = type;
        }
    }

    /**
     * Get the {@link AttributeType} for a static attribute
     * @param staticAttrName of one of the static attribute :
     * <li>{@value #PRIMARY_KEY}
     * <li>{@value #LAST_UPDATE}
     * <li>{@value #LABEL}
     * <li>{@value #RAW_DATA}
     * <li>{@value #THUMBNAIL}
     * <li>{@value #GEOMETRY}
     * @return the {@link AttributeType}
     */
    protected AttributeType getStaticAttributeType(String staticAttrName) {
        switch (staticAttrName) {
            case PRIMARY_KEY:
                return AttributeType.LONG;
            case LABEL:
            case RAW_DATA:
            case THUMBNAIL:
                return AttributeType.STRING;
            case LAST_UPDATE:
                return AttributeType.DATE_ISO8601;
            case GEOMETRY:
                return AttributeType.STRING;
            default:
                return null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getNameDS() {
        return nameDS;
    }

    public void setNameDS(String nameDS) {
        this.nameDS = nameDS;
    }

    public boolean isPrimaryKey() {
        return name.equals(PRIMARY_KEY);
    }

    public boolean isLastUpdate() {
        return name.equals(LAST_UPDATE);
    }

    public boolean isLabel() {
        return name.equals(LABEL);
    }

    public boolean isRawData() {
        return name.equals(RAW_DATA);
    }

    public boolean isThumbnail() {
        return name.equals(THUMBNAIL);
    }

    public boolean isGeometry() {
        return name.equals(GEOMETRY);
    }

    public final boolean isMappedToStaticProperty() {
        return isPrimaryKey() || isLastUpdate() || isLabel() || isRawData() || isThumbnail() || isGeometry();
    }
}
