/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 *
 * TODO description.
 *
 * @author cmertz
 * @since 1.0-SNAPSHOT
 */
public final class Column {

    private Long columnId;

    private String name;

    private int javaSqlType;

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
    public Column(String pName, int pJavaSqlType) {
        super();
        name = pName;
        javaSqlType = pJavaSqlType;
    }

    public Column(String pName, int pJavaSqlType, Boolean pIsPrimaryKey) {
        super();
        name = pName;
        javaSqlType = pJavaSqlType;
        isPrimaryKey = pIsPrimaryKey;
    }

    /**
     * @return the columnId
     */
    public Long getColumnId() {
        return columnId;
    }

    /**
     * @param pColumnId
     *            the columnId to set
     */
    public void setColumnId(Long pColumnId) {
        columnId = pColumnId;
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
     * @return the javaSqlType
     */
    public int getJavaSqlType() {
        return javaSqlType;
    }

    /**
     * @param pJavaSqlType
     *            the javaSqlType to set
     */
    public void setJavaSqlType(int pJavaSqlType) {
        javaSqlType = pJavaSqlType;
    }

    @Override
    public String toString() {
        return "[" + name + " : " + javaSqlType + (isPrimaryKey ? " : is key" : "") + "]";
    }

    /**
     * Get method.
     *
     * @return the isPrimaryKey
     * @since 1.0-SNAPSHOT
     */
    public Boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     *
     * Set method
     *
     * @param pIsPrimaryKey
     * @since 1.0-SNAPSHOT
     */
    public void setPrimaryKey(Boolean pIsPrimaryKey) {
        isPrimaryKey = pIsPrimaryKey;
    }

}
