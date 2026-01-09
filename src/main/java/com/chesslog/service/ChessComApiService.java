package com.chesslog.service;

import com.chesslog.model.ChessGame;
import com.chesslog.model.MonthlyArchive;
import com.chesslog.model.UserArchive;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.github.bhlangonijr.chesslib.pgn.PgnHolder; 
import com.github.bhlangonijr.chesslib.game.Game;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service to fetch PGN archives from the Chess.com Public API using OkHttp and Gson.
 */
public class ChessComApiService {

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private static final String API_BASE_URL = "https://api.chess.com/pub/player/";
    private final ExecutorService executorService = Executors.newFixedThreadPool(5); 

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


    public List<ChessGame> fetchGamesFromArchive(String archiveUrl) {
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


    public List<ChessGame> fetchAllGamesForUser(String username) {
        List<String> archiveUrls = fetchArchiveUrls(username);
        if (archiveUrls.isEmpty()) {
            return Collections.emptyList();
        }

        // Only fetch the most recent archive
        String lastArchiveUrl = archiveUrls.get(archiveUrls.size() - 1);
        List<ChessGame> games = fetchGamesFromArchive(lastArchiveUrl);

        // Limit to the last 20 games
        if (games.size() > 20) {
            games = games.subList(games.size() - 20, games.size());
        }

        for (ChessGame chessGame : games) {
            parsePgnMetadata(chessGame); 
        }

        return games;
    }

    private void parsePgnMetadata(ChessGame chessGame) {
        if (chessGame.getPgn() == null || chessGame.getPgn().isEmpty()) {
            return;
        }

        PgnHolder pgnHolder = new PgnHolder("dummy.pgn");
        pgnHolder.loadPgn(chessGame.getPgn());

        try {
            if (!pgnHolder.getGames().isEmpty()) {
                Game gameFromPgn = pgnHolder.getGames().get(0);
                if (gameFromPgn.getRound() != null && gameFromPgn.getRound().getEvent() != null) {
                    chessGame.event = gameFromPgn.getRound().getEvent().getName();
                    chessGame.site = gameFromPgn.getRound().getEvent().getSite();
                }
                chessGame.date = gameFromPgn.getDate();

                if (gameFromPgn.getWhitePlayer() != null) {
                    chessGame.whitePlayerName = gameFromPgn.getWhitePlayer().getName();
                }
                if (gameFromPgn.getBlackPlayer() != null) {
                    chessGame.blackPlayerName = gameFromPgn.getBlackPlayer().getName();
                }
                if (gameFromPgn.getResult() != null) {
                    chessGame.result = gameFromPgn.getResult().getDescription();
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing PGN: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
