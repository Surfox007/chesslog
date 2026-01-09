package com.chesslog;

import com.chesslog.service.DatabaseService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        DatabaseService.initializeDatabase();

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
