/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;
import org.springframework.data.domain.Pageable;

import com.nurkiewicz.jdbcrepository.sql.SqlGenerator;

/**
 * 
 * @author Christophe Mertz
 *
 */
public class PostgreSqlGenerator extends SqlGenerator {
    
    public PostgreSqlGenerator(String pAllColumnsClause) {
        super(pAllColumnsClause);
    }

    public PostgreSqlGenerator() {
        super("*");
    }

    @Override
    protected String limitClause(Pageable pPage) {
        int offset = pPage.getPageNumber() * pPage.getPageSize();

        return String.format(" LIMIT %d OFFSET %d", pPage.getPageSize(), offset);
    }
}