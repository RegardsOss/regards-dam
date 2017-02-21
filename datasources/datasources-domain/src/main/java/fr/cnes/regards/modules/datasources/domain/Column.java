/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.domain;

import java.sql.Types;

/**
 * This class describes a column of a SQL database.
 *
 * @author Christophe Mertz
 * 
 */
public final class Column {

    /**
     * The column name
     */
    private String name;

    /**
     * The {@link Types} of the column
     */
    private String javaSqlType;

    /**
     * Flag for primary key column. Only one primary key is allowed for a table.<br>
     * Multicolumn primary key is not supported at the moment.
     */
    private Boolean isPrimaryKey = Boolean.FALSE;

    /**
     *
     */
    public Column() {
        super();
    }

    /**
     * @param pName
     *            the name of the column
     * @param pJavaSqlType
     *            the SQL type of the column
     */
    public Column(String pName, String pJavaSqlType) {
        super();
        name = pName;

        javaSqlType = pJavaSqlType;
    }

    /**
     * 
     * @param pName
     *            the name of the column
     * @param pJavaSqlType
     *            the SQL type of the column
     * @param pIsPrimaryKey
     *            the column is the primary key
     */
    public Column(String pName, String pJavaSqlType, Boolean pIsPrimaryKey) {
        super();
        name = pName;
        javaSqlType = pJavaSqlType;
        isPrimaryKey = pIsPrimaryKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getJavaSqlType() {
        return javaSqlType;
    }

    public void setJavaSqlType(String pJavaSqlType) {
        javaSqlType = pJavaSqlType;
    }

    public Boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(Boolean pIsPrimaryKey) {
        isPrimaryKey = pIsPrimaryKey;
    }

    @Override
    public String toString() {
        return "[" + name + " : " + javaSqlType + (isPrimaryKey ? " : is key" : "") + "]";
    }

}
