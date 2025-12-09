package com.chesslog;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.layout.VBox;

public class MainController {

    @FXML
    private VBox boardContainer;

    @FXML
    private Button settingsButton;

    @FXML
    private ToggleButton stockfishToggle;

    @FXML
    private TextArea analysisOutputArea;

    @FXML
    private TextArea moveListArea;

    @FXML private Button firstMoveButton;
    @FXML private Button prevMoveButton;
    @FXML private Button nextMoveButton;
    @FXML private Button lastMoveButton;

    // Save/Delete Buttons
    @FXML private Button deleteButton;
    @FXML private Button saveButton;

    // Load Tab Controls
    @FXML
    private TextField collectionSearchField;

    @FXML
    private TableView savedGamesTable; // Generic TableView for now

    @FXML
    private TextField usernameField;

    @FXML
    private Button importButton;

    @FXML
    private TableView importedGamesTable; // Generic TableView for now
    @FXML
    public void initialize() {
        // ... existing initialization logic ...

        // Example: Link Save/Delete buttons (implementation comes later)
        if (saveButton != null) {
            saveButton.setOnAction(e -> System.out.println("Save analysis clicked."));
        }
        if (deleteButton != null) {
            deleteButton.setOnAction(e -> System.out.println("Delete analysis clicked."));
        }
        if (importButton != null) {
            importButton.setOnAction(e -> System.out.println("Fetching games for user: " + usernameField.getText()));
        }
    }
    // Example handler for settings button
    @FXML
    private void handleSettings() {
        System.out.println("Settings button clicked!");
        // Logic to open settings window
    }
}