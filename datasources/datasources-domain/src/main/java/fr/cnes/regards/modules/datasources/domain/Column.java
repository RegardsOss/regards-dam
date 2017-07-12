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
