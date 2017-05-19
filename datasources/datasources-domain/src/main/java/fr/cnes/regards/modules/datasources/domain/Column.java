/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.domain;

import java.sql.Types;

import fr.cnes.regards.framework.gson.annotation.GsonIgnore;

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

    @GsonIgnore
    private Integer sqlType;

    /**
     *
     */
    public Column() {
        super();
    }

    public Column(String pName, String pJavaSqlType, Integer pSqlType) {
        super();
        name = pName;
        javaSqlType = pJavaSqlType;
        this.sqlType = pSqlType;
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

    public Integer getSqlType() {
        return sqlType;
    }

    public void setSqlType(Integer sqlType) {
        this.sqlType = sqlType;
    }

    @Override
    public String toString() {
        return "[" + name + " : " + javaSqlType + "]";
    }

}
