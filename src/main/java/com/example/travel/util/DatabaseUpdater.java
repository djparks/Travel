package com.example.travel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class to update the database schema.
 */
public class DatabaseUpdater {
    private static final String DB_URL = "jdbc:h2:file:./traveldb";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    /**
     * Updates the database schema to add the visited and plan columns.
     * This method should be called when the application starts.
     */
    public static void updateSchema() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (Statement stmt = conn.createStatement()) {
                // Add visited column if it doesn't exist
                try {
                    stmt.execute("ALTER TABLE travel_records ADD COLUMN visited BOOLEAN DEFAULT FALSE NOT NULL");
                    System.out.println("Added 'visited' column to travel_records table");
                } catch (SQLException e) {
                    // Column might already exist, which is fine
                    if (e.getMessage().toLowerCase().contains("duplicate column name")) {
                        System.out.println("Column 'visited' already exists");
                    } else {
                        throw e;
                    }
                }

                // Add plan column if it doesn't exist
                try {
                    stmt.execute("ALTER TABLE travel_records ADD COLUMN plan BOOLEAN DEFAULT FALSE NOT NULL");
                    System.out.println("Added 'plan' column to travel_records table");
                } catch (SQLException e) {
                    // Column might already exist, which is fine
                    if (e.getMessage().toLowerCase().contains("duplicate column name")) {
                        System.out.println("Column 'plan' already exists");
                    } else {
                        throw e;
                    }
                }

                // Add phone_number column if it doesn't exist
                try {
                    stmt.execute("ALTER TABLE travel_records ADD COLUMN phone_number VARCHAR(20)");
                    System.out.println("Added 'phone_number' column to travel_records table");
                } catch (SQLException e) {
                    // Column might already exist, which is fine
                    if (e.getMessage().toLowerCase().contains("duplicate column name")) {
                        System.out.println("Column 'phone_number' already exists");
                    } else {
                        throw e;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
