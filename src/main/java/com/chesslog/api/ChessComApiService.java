package com.chesslog.api;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

//Service to fetch PGN archives from the Chess.com Public API using OkHttp and Gson.
public class ChessComApiService {

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private static final String API_BASE_URL = "https://api.chess.com/pub/player/";

    /**
     * Fetches all monthly archive URLs for a given Chess.com username.
     * @param username The Chess.com username.
     * @return A list of monthly archive URLs.
     */
    public List<String> fetchArchiveUrls(String username) {
        String url = API_BASE_URL + username.toLowerCase() + "/games/archives";

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                System.err.println("Failed to fetch archives for " + username + ". Status: " + response.code());
                return Collections.emptyList();
            }

            String json = response.body().string();

            UserArchive userArchive = gson.fromJson(json, UserArchive.class);
            return userArchive.getArchiveUrls();

        } catch (IOException e) {
            System.err.println("Network error fetching archive URLs: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetches all games from a single monthly archive URL.
     * @param archiveUrl The URL of the monthly archive (e.g., .../2023/01).
     * @return A list of Game objects containing the raw PGN.
     */
    public List<Game> fetchGamesFromArchive(String archiveUrl) {
        Request request = new Request.Builder().url(archiveUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                System.err.println("Failed to fetch games from archive: " + archiveUrl);
                return Collections.emptyList();
            }

            String json = response.body().string();

            MonthlyArchive monthlyArchive = gson.fromJson(json, MonthlyArchive.class);
            return monthlyArchive.getGames();

        } catch (IOException e) {
            System.err.println("Network error fetching monthly archive: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}