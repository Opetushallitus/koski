package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import fi.vm.sade.oidgenerator.OIDGenerator;

public class V39__Generate_opiskeluoikeus_oid implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        connection.setAutoCommit(false);
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id FROM opiskeluoikeus")) {
            updateOids(connection, stmt.executeQuery());
        }
        connection.commit();
    }

    private void updateOids(Connection connection, ResultSet rs) throws SQLException {
        try {
            while (rs.next()) {
                updateOid(rs.getInt(1), connection.prepareStatement("UPDATE opiskeluoikeus SET oid = ? WHERE id = ?"));
            }
        } finally {
            rs.close();
        }
    }

    private void updateOid(int id, PreparedStatement stmt) throws SQLException {
        try {
            stmt.setString(1, OIDGenerator.generateOID(15));
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } finally {
            stmt.close();
        }
    }
}
