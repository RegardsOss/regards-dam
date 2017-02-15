/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.utils;

import java.util.HashSet;

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
    private String pkColumn;

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

    public String getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(String pColumn) {
        this.pkColumn = pColumn;
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
        return pVal != null && pVal != "" ? pName + "=" + pVal : "";
    }

    @Override
    public String toString() {
        return "[TABLE :" + name + "]" + " : " + toString("schema", schema) + " : " + toString("catalog", catalog)
                + " : " + pkColumn != null ? "pkKey=" + pkColumn : "";
    }

}
