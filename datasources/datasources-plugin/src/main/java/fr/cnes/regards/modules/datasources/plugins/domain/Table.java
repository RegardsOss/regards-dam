/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.domain;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO description.
 *
 * @author cmertz
 * @since 1.0-SNAPSHOT
 */
public final class Table {

    private String name;

    private String alias;

    private List<Column> columns = null;

    private String pkName;

    private String schema;

    /**
     * @param pName
     *            the name of the table
     */
    public Table(String pName) {
        super();
        name = pName;
        columns = new ArrayList<Column>();
    }

    /**
     * @param pName
     *            the name of the table
     * @param pSchema
     *            the schema of the database
     */
    public Table(String pName, String pSchema) {
        super();
        name = pName;
        schema = pSchema;
        columns = new ArrayList<Column>();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param pName
     *            the name to set
     */
    public void setName(String pName) {
        name = pName;
    }

    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param pAlias
     *            the alias to set
     */
    public void setAlias(String pAlias) {
        alias = pAlias;
    }

    /**
     * @return the attributes
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * @param pColumns
     *            the attributes to set
     */
    public void setColumns(List<Column> pColumns) {
        columns = pColumns;
    }

    public void addColumn(Column pColumn) {
        this.columns.add(pColumn);
    }

    /**
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @param pSchema
     *            the schema to set
     */
    public void setSchema(String pSchema) {
        schema = pSchema;
    }

    /**
     * Get the table reference
     *
     * @return the table referenced
     */
    public String getTableDefinition() {
        return schema != null ? schema + "." + name : name;
    }

    /**
     *
     * Declare a new column to the current table
     *
     * @param pName
     *            the column name
     * @param pJavaSqlType
     *            the SQL type @see {@link Types}
     * @param pIsPrimaryKey
     *            if this column is the primary key
     * @since 1.0-SNAPSHOT
     */
    public void addColumn(String pName, int pJavaSqlType, Boolean pIsPrimaryKey) {
        this.columns.add(new Column(pName, pJavaSqlType, pIsPrimaryKey));
        if (pIsPrimaryKey) {
            this.pkName = pName;
        }
    }

    /**
     *
     * Declare a new column to the current table
     *
     * @param pName
     *            the column name
     * @param pJavaSqlType
     *            the SQL type @see {@link Types}
     * @since 1.0-SNAPSHOT
     */
    public void addColumn(String pName, int pJavaSqlType) {
        addColumn(pName, pJavaSqlType, Boolean.FALSE);
    }

    public void addColumnString(String pName) {
        addColumn(pName, Types.VARCHAR);
    }

    public void addColumnString(String pName, Boolean pIsPrimaryKey) {
        addColumn(pName, Types.VARCHAR, pIsPrimaryKey);
    }

    public void addColumnInteger(String pName) {
        addColumn(pName, Types.INTEGER);
    }

    public void addColumnInteger(String pName, Boolean pIsPrimaryKey) {
        addColumn(pName, Types.INTEGER, pIsPrimaryKey);
    }

    public void addColumnLong(String pName) {
        addColumn(pName, Types.BIGINT);
    }

    public void addColumnLong(String pName, Boolean pIsPrimaryKey) {
        addColumn(pName, Types.BIGINT, pIsPrimaryKey);
    }

    public void addColumnDate(String pName) {
        addColumn(pName, Types.DATE);
    }

    public void addColumnDate(String pName, Boolean pIsPrimaryKey) {
        addColumn(pName, Types.DATE, pIsPrimaryKey);
    }

    @Override
    public String toString() {
        return "[TABLE :" + name + "]" + " - " + alias != null && alias != "" ? "alias:" + alias
                : "" + " - " + pkName != null && pkName != "" ? "idkey=" + pkName
                        : "" + " - " + "attribute=" + columns.size();
    }

    /**
     * Get method.
     *
     * @return the pkName
     * @since TODO
     */
    public String getPkName() {
        return pkName;
    }
}
