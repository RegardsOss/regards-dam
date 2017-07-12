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

package fr.cnes.regards.modules.datasources.utils;

import org.springframework.data.domain.Pageable;

import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;

/**
 * 
 * @author Christophe Mertz
 *
 */
public class PostgreSqlGenerator extends SqlGenerator {

    /**
     * The table name used in a "ORDER BY" clause
     */
    private String orderByTable;

    public PostgreSqlGenerator(String pAllColumnsClause, String pOrderTable) {
        super(pAllColumnsClause);
        this.orderByTable = pOrderTable;
    }

    public PostgreSqlGenerator() {
        super("*");
    }

    @Override
    protected String limitClause(Pageable pPage) {
        int offset = pPage.getPageNumber() * pPage.getPageSize();

        if (orderByTable != null && !orderByTable.isEmpty()) {
            return String.format(" ORDER BY %s LIMIT %d OFFSET %d", orderByTable, pPage.getPageSize(), offset);

        } else {
            return String.format(" LIMIT %d OFFSET %d", pPage.getPageSize(), offset);
        }

    }
}