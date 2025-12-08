package com.chesslog;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML private VBox sidebar;
    @FXML private VBox navMenu;

    @FXML
    public void initialize() {
        // Initialization logic (e.g., setting up event handlers) can go here
        System.out.println("MainController initialized.");

        // Add click handlers or hover effects dynamically if needed
        navMenu.getChildren().forEach(node -> {
            if (node instanceof Label) {
                Label item = (Label) node;
                // Add a simple click/selection effect
                item.setOnMouseClicked(event -> {
                    System.out.println("Clicked: " + item.getText());
                    // Logic to switch the center view goes here
                });
            }
        });
    }
}