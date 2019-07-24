/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.domain.datasources;

/**
 * This class describes a table of a SQL database.
 *
 * @author Christophe Mertz
 * 
 */
public final class Table {

    /**
     * The table name
     */
    private String name;

    /**
     * a catalog name; must match the catalog name as it is stored in the database
     */
    private String catalog;

    /**
     * a schema name; must match the schema name as it is stored in the database
     */
    private String schema;

    /**
     * The column that is the primary key
     */
    private String pKey;

    /**
     * @param pName
     *            the name of the table
     */
    public Table(String pName) {
        super();
        name = pName;
    }

    /**
     * @param pName
     *            the name of the table
     * @param pSchema
     *            the schema name stored in the database
     */
    public Table(String pName, String pSchema) {
        super();
        name = pName;
        schema = pSchema;
    }

    /**
     * @param pName
     *            the name of the table
     * @param pCatalog
     *            the catalog name stored in the database
     * @param pSchema
     *            the schema stored stored in the database
     */
    public Table(String pName, String pCatalog, String pSchema) {
        super();
        name = pName;
        schema = pSchema;
        catalog = pCatalog;
    }

    /**
     * Get the table reference
     *
     * @return the table referenced
     */
    public String getTableDefinition() {
        return schema != null ? schema + "." + name : name;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        this.name = pName;
    }

    public String getPKey() {
        return pKey;
    }

    public void setPKey(String pKey) {
        this.pKey = pKey;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String pSchema) {
        this.schema = pSchema;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String pCatalog) {
        this.catalog = pCatalog;
    }

    private String toString(String pName, String pVal) {
        return (pVal != null && !pVal.isEmpty()) ? (pName + "=" + pVal) : "";
    }

    @Override
    public String toString() {
        return "[TABLE :" + name + "]" + " : " + toString("schema", schema) + " : " + toString("catalog", catalog)
                + " : " + (pKey != null ? "pKey=" + pKey : "");
    }

}
