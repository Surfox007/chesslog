package com.chesslog.service;

import com.chesslog.model.ChessGame;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Manages the connection and schema creation for the local SQLite database.
public class DatabaseService {

    private static final String DB_NAME = "chesslog.db";

    private static final String JDBC_URL = "jdbc:sqlite:" + DB_NAME;


    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(JDBC_URL);
    }


    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

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

    public void saveGame(ChessGame game, String username) throws SQLException {
        String sql = """
        INSERT INTO games (pgn_id, username, event, site, date, round, white, black, result, eco, termination, time_control, white_elo, black_elo, pgn)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(pgn_id) DO NOTHING;
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, game.getUrl());
            pstmt.setString(2, username);
            pstmt.setString(3, parsePgnTag(game.getPgn(), "Event"));
            pstmt.setString(4, parsePgnTag(game.getPgn(), "Site"));
            pstmt.setString(5, parsePgnTag(game.getPgn(), "Date"));
            pstmt.setString(6, parsePgnTag(game.getPgn(), "Round"));
            pstmt.setString(7, parsePgnTag(game.getPgn(), "White"));
            pstmt.setString(8, parsePgnTag(game.getPgn(), "Black"));
            pstmt.setString(9, parsePgnTag(game.getPgn(), "Result"));
            pstmt.setString(10, parsePgnTag(game.getPgn(), "ECO"));
            pstmt.setString(11, parsePgnTag(game.getPgn(), "Termination"));
            pstmt.setString(12, parsePgnTag(game.getPgn(), "TimeControl"));
            pstmt.setInt(13, Integer.parseInt(parsePgnTag(game.getPgn(), "WhiteElo", "0")));
            pstmt.setInt(14, Integer.parseInt(parsePgnTag(game.getPgn(), "BlackElo", "0")));
            pstmt.setString(15, game.getPgn());

            pstmt.executeUpdate();
        }
    }

    public List<ChessGame> getAllGames() throws SQLException {
        List<ChessGame> games = new ArrayList<>();
        String sql = "SELECT * FROM games ORDER BY date DESC;";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ChessGame game = new ChessGame();
                game.url = rs.getString("pgn_id");
                game.event = rs.getString("event");
                game.site = rs.getString("site");
                game.date = rs.getString("date");
                game.whitePlayerName = rs.getString("white");
                game.blackPlayerName = rs.getString("black");
                game.result = rs.getString("result");
                game.pgn = rs.getString("pgn");
                games.add(game);
            }
        }
        return games;
    }

    private String parsePgnTag(String pgn, String tagName) {
        return parsePgnTag(pgn, tagName, "");
    }

    private String parsePgnTag(String pgn, String tagName, String defaultValue) {
        if (pgn == null || tagName == null) {
            return defaultValue;
        }
        // Matches [TagName "Value"]
        Pattern pattern = Pattern.compile("\\[" + tagName + "\\s+\"(.*?)\"\\]");
        Matcher matcher = pattern.matcher(pgn);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return defaultValue;
    }

    public void deleteGame(String url) throws SQLException {
        String sql = "DELETE FROM games WHERE pgn_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, url);
            pstmt.executeUpdate();
        }
    }

    public boolean isGameSaved(String url) throws SQLException {
        String sql = "SELECT 1 FROM games WHERE pgn_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, url);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}