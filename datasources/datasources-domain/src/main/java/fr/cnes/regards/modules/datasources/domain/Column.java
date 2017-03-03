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
     *
     */
    public Column() {
        super();
    }

    /**
     * 
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

    @Override
    public String toString() {
        return "[" + name + " : " + javaSqlType + "]";
    }

}
