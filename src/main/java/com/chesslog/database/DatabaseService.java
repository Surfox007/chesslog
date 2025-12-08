package com.chesslog.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

//Manages the connection and schema creation for the local SQLite database.
public class DatabaseService {

    private static final String DB_NAME = "chesslog.db";
    // JDBC URL for SQLite: jdbc:sqlite:/path/to/database.db
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_NAME;

    /**
     * Establishes a connection to the SQLite database. Creates the database file
     * if it does not exist.
     * @return A valid Connection object.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        // SQLite will automatically create the file if it doesn't exist
        return DriverManager.getConnection(JDBC_URL);
    }

    //Initializes the database schema (creates tables if they don't exist)
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // SQL to create the Games table.
            String sql = """
                CREATE TABLE IF NOT EXISTS games (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pgn_id TEXT NOT NULL UNIQUE,       -- Unique ID based on PGN data
                    username TEXT NOT NULL,            -- The Chess.com username
                    event TEXT,                        -- E.g., Live Chess, Daily Match
                    site TEXT,
                    date TEXT,
                    round TEXT,
                    white TEXT,
                    black TEXT,
                    result TEXT,
                    eco TEXT,                          -- Encyclopedia of Chess Openings
                    termination TEXT,
                    time_control TEXT,
                    white_elo INTEGER,
                    black_elo INTEGER,
                    pgn TEXT NOT NULL                  -- The full PGN string (moves and headers)
                );
                """;

            stmt.execute(sql);
            System.out.println("Database and 'games' table initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}