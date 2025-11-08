package com.example.leavemanagement.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class DbInspector implements CommandLineRunner {

    private final DataSource dataSource;

    public DbInspector(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            // Ensure admin password is encoded with BCrypt (if DB contains plain-text)
            try (Statement st = conn.createStatement()) {
                try (ResultSet r = st.executeQuery("SELECT password FROM users WHERE username = 'admin' LIMIT 1")) {
                    if (r.next()) {
                        String pw = r.getString(1);
                        if (pw != null && !pw.startsWith("$2a$") && !pw.startsWith("$2b$") && !pw.startsWith("$2y$")) {
                            String encoded = new BCryptPasswordEncoder().encode(pw);
                            int updated = st.executeUpdate("UPDATE users SET password='" + encoded + "' WHERE username='admin'");
                            System.out.println("DbInspector: updated admin password to BCrypt (rows updated=" + updated + ")");
                        }
                    }
                } catch (Exception ignore) {
                    // table may not exist yet or column different; ignore
                }
            } catch (Exception ignore) {
            }

            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                System.out.println("=== DATABASE TABLES ===");
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    System.out.println("Table: " + tableName);
                    try (Statement st = conn.createStatement()) {
                        try (ResultSet r2 = st.executeQuery("SELECT COUNT(*) FROM `" + tableName + "`")) {
                            if (r2.next()) {
                                System.out.println("  Rows: " + r2.getInt(1));
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("  (could not count rows: " + e.getMessage() + ")");
                    }
                }
                System.out.println("=== END TABLE LIST ===");
            }
        } catch (Exception e) {
            System.out.println("DbInspector: could not inspect database: " + e.getMessage());
        }
    }
}
