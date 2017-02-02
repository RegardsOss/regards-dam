/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.domain;

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

    // /**
    // * The {@link Column} of the table
    // */
    // private Set<Column> columns = null;

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
        // columns = new HashSet<>();
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
        // columns = new HashSet<>();
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
        // columns = new HashSet<>();
    }

    /**
     * Get the table reference
     *
     * @return the table referenced
     */
    public String getTableDefinition() {
        return schema != null ? schema + "." + name : name;
    }

    // /**
    // *
    // * Declare a new column to the current table
    // *
    // * @param pName
    // * the column name
    // * @param pJavaSqlType
    // * the SQL type @see {@link Types}
    // * @param pIsPrimaryKey
    // * if this column is the primary key
    // */
    // public void addColumn(String pName, int pJavaSqlType, Boolean pIsPrimaryKey) {
    // Column col = new Column(pName, pJavaSqlType, pIsPrimaryKey);
    // this.columns.add(col);
    // if (pIsPrimaryKey) {
    // this.pkColumn = col;
    // }
    // }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // public Set<Column> getColumns() {
    // return columns;
    // }
    //
    // public void setColumns(Set<Column> columns) {
    // this.columns = columns;
    // }

    public String getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(String pkColumn) {
        this.pkColumn = pkColumn;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    // /**
    // *
    // * Declare a new column to the current table
    // *
    // * @param pName
    // * the column name
    // * @param pJavaSqlType
    // * the SQL type @see {@link Types}
    // */
    // public void addColumn(String pName, int pJavaSqlType) {
    // addColumn(pName, pJavaSqlType, Boolean.FALSE);
    // }
    //
    // public void addColumnString(String pName) {
    // addColumn(pName, Types.VARCHAR);
    // }
    //
    // public void addColumnString(String pName, Boolean pIsPrimaryKey) {
    // addColumn(pName, Types.VARCHAR, pIsPrimaryKey);
    // }
    //
    // public void addColumnInteger(String pName) {
    // addColumn(pName, Types.INTEGER);
    // }
    //
    // public void addColumnInteger(String pName, Boolean pIsPrimaryKey) {
    // addColumn(pName, Types.INTEGER, pIsPrimaryKey);
    // }
    //
    // public void addColumnLong(String pName) {
    // addColumn(pName, Types.BIGINT);
    // }
    //
    // public void addColumnLong(String pName, Boolean pIsPrimaryKey) {
    // addColumn(pName, Types.BIGINT, pIsPrimaryKey);
    // }
    //
    // public void addColumnDate(String pName) {
    // addColumn(pName, Types.DATE);
    // }
    //
    // public void addColumnDate(String pName, Boolean pIsPrimaryKey) {
    // addColumn(pName, Types.DATE, pIsPrimaryKey);
    // }

    private String toString(String pName, String pVal) {
        return pVal != null && pVal != "" ? pName + "=" + pVal : "";
    }

    @Override
    public String toString() {
        return "[TABLE :" + name + "]" + " : " + toString("schema", schema) + " : " + toString("catalog", catalog)
                + " : " + pkColumn != null ? "pkKey=" + pkColumn : "";
    }

}
