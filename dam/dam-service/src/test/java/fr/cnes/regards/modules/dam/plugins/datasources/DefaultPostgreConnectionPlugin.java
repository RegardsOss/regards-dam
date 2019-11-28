package fr.cnes.regards.modules.dam.plugins.datasources;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.dam.domain.datasources.Column;
import fr.cnes.regards.modules.dam.domain.datasources.Table;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDBConnectionPlugin;

/**
 * @author Sylvain VISSIERE-GUERINET
 */
@Plugin(id = "test-db-connection", version = "TEST", description = "Test implementation should not be used",
        author = "REGARDS Team", contact = "regards@c-s.fr", license = "GPLv3", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class DefaultPostgreConnectionPlugin implements IDBConnectionPlugin {

    @Override
    public boolean testConnection() {
        throw new UnsupportedOperationException("This is a stub for plugin discovery, in no case should it be used!");
    }

    @Override
    public void closeConnection() {
        throw new UnsupportedOperationException("This is a stub for plugin discovery, in no case should it be used!");
    }

    @Override
    public Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException("This is a stub for plugin discovery, in no case should it be used!");
    }

    @Override
    public Map<String, Table> getTables(String schemaPattern, String tableNamePattern) {
        throw new UnsupportedOperationException("This is a stub for plugin discovery, in no case should it be used!");
    }

    @Override
    public Map<String, Column> getColumns(String tableName) {
        throw new UnsupportedOperationException("This is a stub for plugin discovery, in no case should it be used!");
    }
}
