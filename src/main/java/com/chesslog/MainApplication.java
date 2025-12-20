package com.chesslog;

import com.chesslog.api.ChessComApiService;
import com.chesslog.api.ChessGame;
import com.chesslog.database.DatabaseService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        DatabaseService.initializeDatabase();

        //TEMPORARY API TEST
        String testUser = "ObliviousPeregrine";
        ChessComApiService apiService = new ChessComApiService();

        try {
            // 1. Fetch archives
            List<String> urls = apiService.fetchArchiveUrls(testUser);
            System.out.println("Fetched " + urls.size() + " archive URLs for " + testUser);

            // 2. Fetch games from the MOST RECENT archive (to limit the load)
            if (!urls.isEmpty()) {
                String latestUrl = urls.get(urls.size() - 1);
                List<ChessGame> games = apiService.fetchGamesFromArchive(latestUrl);
                System.out.println("Fetched " + games.size() + " games from latest archive: " + latestUrl);
            }
        } catch (Exception e) {
            System.err.println("API Test failed: " + e.getMessage());
        }
        // ===================================

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/com/chesslog/MainView.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 1000, 750);

        stage.setTitle("ChessLog Desktop");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}