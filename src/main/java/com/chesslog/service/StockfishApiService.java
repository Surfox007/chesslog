package com.chesslog.service;

import com.chesslog.model.AnalysisLine;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to interact with the stockfish.online API.
 */
public class StockfishApiService {
    private static final String API_URL = "https://stockfish.online/api/s/v2.php";

    /**
     * Sends a FEN string to the Stockfish API and returns the analysis results as a list of lines.
     *
     * @param fen The chess FEN string.
     * @return A list of AnalysisLine objects.
     */
    public List<AnalysisLine> getAnalysisLines(String fen) {
        List<AnalysisLine> lines = new ArrayList<>();
        JsonObject json = getAnalysis(fen);
        
        if (json != null && json.has("success") && json.get("success").getAsBoolean()) {
            String evaluation = "0.00";
            boolean isMate = false;
            
            if (json.has("mate") && !json.get("mate").isJsonNull()) {
                evaluation = json.get("mate").getAsString();
                isMate = true;
            } else if (json.has("evaluation")) {
                evaluation = json.get("evaluation").getAsString();
            }
            
            String bestMoveRaw = json.has("bestmove") ? json.get("bestmove").getAsString() : "";
            // Extract actual best move (e.g. from "bestmove e2e4 ponder e7e5" get "e2e4")
            String bestMove = bestMoveRaw.replace("bestmove ", "").split(" ")[0];
            
            String continuation = json.has("continuation") ? json.get("continuation").getAsString() : "";
            
            // Remove the best move from the continuation string to avoid redundancy if needed, 
            // but the user wants "Best Move" then "Continuation".
            // Actually, continuation usually includes the best move.
            
            lines.add(new AnalysisLine(evaluation, bestMove, continuation, isMate));
        }
        
        return lines;
    }

    /**
     * Sends a FEN string to the Stockfish API and returns the best move.
     * 
     * @param fen The chess FEN string.
     * @return The best move string (e.g., "e2e4"), or null if the request fails.
     */
    public String getBestMove(String fen) {
        try {
            String encodedFen = URLEncoder.encode(fen, StandardCharsets.UTF_8);
            URL url = new URL(API_URL + "?fen=" + encodedFen + "&depth=15");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    
                    JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                    if (jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
                        // The API v2 returns the best move in the "bestmove" field
                        if (jsonObject.has("bestmove")) {
                            return jsonObject.get("bestmove").getAsString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * A more comprehensive call that returns the full JSON response if needed.
     */
    public JsonObject getAnalysis(String fen) {
        try {
            String encodedFen = URLEncoder.encode(fen, StandardCharsets.UTF_8);
            URL url = new URL(API_URL + "?fen=" + encodedFen + "&depth=15");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return JsonParser.parseString(response.toString()).getAsJsonObject();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
