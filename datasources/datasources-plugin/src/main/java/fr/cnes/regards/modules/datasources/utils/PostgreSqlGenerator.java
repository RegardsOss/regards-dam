/*
 * LICENSE_PLACEHOLDER
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