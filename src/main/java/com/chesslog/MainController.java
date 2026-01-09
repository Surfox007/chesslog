package com.chesslog;

import com.chesslog.model.AnalysisLine;
import com.chesslog.service.ChessComApiService;
import com.chesslog.model.ChessGame;
import com.chesslog.service.StockfishApiService;
import com.chesslog.service.DatabaseService;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.MoveList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML
    private VBox boardContainer;

    @FXML
    private Button settingsButton;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private ToggleButton stockfishToggle;

    @FXML
    private Label depthLabel;

    @FXML
    private ListView<AnalysisLine> engineMovesListView;

    @FXML
    private TextFlow moveListArea;

    @FXML private Button firstMoveButton;
    @FXML private Button prevMoveButton;
    @FXML private Button nextMoveButton;
    @FXML private Button lastMoveButton;

    @FXML private ToggleButton saveStarButton;

    @FXML
    private TextField collectionSearchField;

    @FXML
    private TableView<ChessGame> savedGamesTable;

    @FXML
    private TextField usernameField;

    @FXML
    private Button importButton;

    @FXML
    private TableView<ChessGame> importedGamesTable;

    @FXML private Label blackPlayerIcon;
    @FXML private Label blackPlayerNameLabel;
    @FXML private Label whitePlayerIcon;
    @FXML private Label whitePlayerNameLabel;


    private final ChessComApiService chessComApiService = new ChessComApiService();
    private final DatabaseService databaseService = new DatabaseService();
    private final StockfishApiService stockfishApiService = new StockfishApiService();
    private final BooleanProperty isStockfishRunning = new SimpleBooleanProperty(false);

    private ChessGame currentlyLoadedGame;
    private Board board;
    private MoveList moveList;
    private int currentMoveIndex = -1;
    private Chessboard chessboard;
    private List<Text> moveTextNodes = new ArrayList<>();

    @FXML
    public void initialize() {
        if (importButton != null) {
            importButton.setOnAction(e -> handleImportButtonAction());
        }
        if (saveStarButton != null) {
            saveStarButton.setOnAction(e -> handleSaveGame());
            saveStarButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    saveStarButton.setText("★");
                } else {
                    saveStarButton.setText("☆");
                }
            });
        }

        // Setup navigation button handlers
        if (firstMoveButton != null) {
            firstMoveButton.setOnAction(e -> handleFirstMove());
        }
        if (prevMoveButton != null) {
            prevMoveButton.setOnAction(e -> handlePrevMove());
        }
        if (nextMoveButton != null) {
            nextMoveButton.setOnAction(e -> handleNextMove());
        }
        if (lastMoveButton != null) {
            lastMoveButton.setOnAction(e -> handleLastMove());
        }

        this.board = new Board();
        chessboard = new Chessboard();
        chessboard.setBoard(this.board);

        if (boardContainer != null) {
            boardContainer.getChildren().add(1, chessboard);
        }
        clearPlayerInfo();
        setupImportedGamesTable();
        setupSavedGamesTable();
        loadSavedGames();
        updateSaveStarState();
        updateNavigationButtonsState();

        if (stockfishToggle != null) {
            stockfishToggle.selectedProperty().bindBidirectional(isStockfishRunning);
            isStockfishRunning.addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    analyzeCurrentPosition();
                } else {
                    clearAnalysis();
                }
            });
        }

        setupEngineMovesListView();
    }

    private void setupEngineMovesListView() {
        if (engineMovesListView == null) return;

        engineMovesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(AnalysisLine line, boolean empty) {
                super.updateItem(line, empty);
                if (empty || line == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);

                    Label scoreLabel = new Label(line.getFormattedEvaluation());
                    scoreLabel.getStyleClass().add("engine-score-badge");
                    
                    // Determine color based on score
                    try {
                        double eval = Double.parseDouble(line.getEvaluation());
                        if (eval > 0.5) {
                            scoreLabel.setStyle("-fx-background-color: #769656; -fx-text-fill: white;");
                        } else if (eval < -0.5) {
                            scoreLabel.setStyle("-fx-background-color: #b33434; -fx-text-fill: white;");
                        } else {
                            scoreLabel.setStyle("-fx-background-color: #808080; -fx-text-fill: white;");
                        }
                    } catch (Exception e) {
                        if (line.isMate()) {
                            scoreLabel.setStyle("-fx-background-color: #000000; -fx-text-fill: white;");
                        }
                    }

                    Label bestMoveLabel = new Label(line.getBestMove());
                    bestMoveLabel.setStyle("-fx-font-weight: bold;");

                    Label continuationLabel = new Label(line.getContinuation());
                    continuationLabel.setStyle("-fx-text-fill: grey; -fx-font-size: 11px;");
                    continuationLabel.setWrapText(true);
                    HBox.setHgrow(continuationLabel, Priority.ALWAYS);

                    container.getChildren().addAll(scoreLabel, bestMoveLabel, continuationLabel);
                    setGraphic(container);
                }
            }
        });
    }

    private void clearPlayerInfo() {
        if (blackPlayerIcon != null) {
            blackPlayerIcon.setText("⚫");
        }
        if (blackPlayerNameLabel != null) {
            blackPlayerNameLabel.setText("[Black Player]");
        }
        if (whitePlayerIcon != null) {
            whitePlayerIcon.setText("⚪");
        }
        if (whitePlayerNameLabel != null) {
            whitePlayerNameLabel.setText("[White Player]");
        }
    }

    private void setupImportedGamesTable() {
        if (importedGamesTable == null) {
            return;
        }
        importedGamesTable.getColumns().clear();
        TableColumn<ChessGame, String> whitePlayerCol = new TableColumn<>("White");
        whitePlayerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getWhitePlayerName()));
        importedGamesTable.getColumns().add(whitePlayerCol);

        TableColumn<ChessGame, String> blackPlayerCol = new TableColumn<>("Black");
        blackPlayerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBlackPlayerName()));
        importedGamesTable.getColumns().add(blackPlayerCol);

        TableColumn<ChessGame, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        importedGamesTable.getColumns().add(dateCol);

        TableColumn<ChessGame, String> resultCol = new TableColumn<>("Result");
        resultCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getResult()));
        importedGamesTable.getColumns().add(resultCol);

        TableColumn<ChessGame, Void> loadButtonCol = new TableColumn<>("Load");
        loadButtonCol.setCellFactory(param -> new TableCell<>() {
            private final Button loadBtn = new Button("Load");
            {
                loadBtn.setOnAction(event -> {
                    ChessGame game = getTableView().getItems().get(getIndex());
                    loadGame(game);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : loadBtn);
            }
        });
        importedGamesTable.getColumns().add(loadButtonCol);
    }

    private void setupSavedGamesTable() {
        if (savedGamesTable == null) {
            return;
        }
        savedGamesTable.getColumns().clear();
        TableColumn<ChessGame, String> whitePlayerCol = new TableColumn<>("White");
        whitePlayerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getWhitePlayerName()));
        savedGamesTable.getColumns().add(whitePlayerCol);

        TableColumn<ChessGame, String> blackPlayerCol = new TableColumn<>("Black");
        blackPlayerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBlackPlayerName()));
        savedGamesTable.getColumns().add(blackPlayerCol);

        TableColumn<ChessGame, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        savedGamesTable.getColumns().add(dateCol);

        TableColumn<ChessGame, String> resultCol = new TableColumn<>("Result");
        resultCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getResult()));
        savedGamesTable.getColumns().add(resultCol);

        TableColumn<ChessGame, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button loadBtn = new Button("Load");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, loadBtn, deleteBtn);

            {
                loadBtn.setOnAction(event -> {
                    ChessGame game = getTableView().getItems().get(getIndex());
                    loadGame(game);
                });
                deleteBtn.setOnAction(event -> {
                    ChessGame game = getTableView().getItems().get(getIndex());
                    handleDeleteGameFromCollection(game);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        savedGamesTable.getColumns().add(actionsCol);
    }

    private void loadGame(ChessGame gameToLoad) {

        this.currentlyLoadedGame = gameToLoad;
        this.board = new Board();
        this.moveList = new MoveList();
        this.currentMoveIndex = -1;

        if (blackPlayerNameLabel != null) {
            blackPlayerNameLabel.setText(gameToLoad.getBlackPlayerName());
        }
        if (whitePlayerNameLabel != null) {
            whitePlayerNameLabel.setText(gameToLoad.getWhitePlayerName());
        }

        String pgn = currentlyLoadedGame.getPgn();
        if (pgn == null || pgn.isEmpty()) {
            showAlert("Error", "Game data is missing PGN.", Alert.AlertType.ERROR);
            return;
        }

        String sanMoves = "";
        int moveTextStart = pgn.indexOf("1.");
        if (moveTextStart != -1) {
            sanMoves = pgn.substring(moveTextStart);
        } else {
            String[] lines = pgn.split("\n");
            for (String line : lines) {
                if (!line.startsWith("[") && line.trim().length() > 0) {
                    sanMoves += line + "\n";
                }
            }
        }

        if (sanMoves.isEmpty()) {
            showAlert("Error", "Could not parse PGN moves.", Alert.AlertType.ERROR);
            return;
        }

        sanMoves = sanMoves.replaceAll("\\{[^}]*\\}", ""); // Remove comments
        sanMoves = sanMoves.replace("1-0", "").replace("0-1", "").replace("1/2-1/2", "").replace("*", "").trim();

        try {
            moveList.loadFromSan(sanMoves);
            board.loadFromFen(moveList.getStartFen());
            updateBoardView();
            updateSaveStarState();
            updateMoveListArea();
            updateNavigationButtonsState();


            if (mainTabPane != null) {
                mainTabPane.getSelectionModel().select(0);
            }
            if (isStockfishRunning.get()) {
                analyzeCurrentPosition();
            }

        } catch (Exception e) {
            showAlert("PGN Parse Error", "Failed to load moves from PGN: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    private void updateMoveListArea() {
        if (moveListArea == null || moveList == null) {
            return;
        }
        moveListArea.getChildren().clear();
        moveTextNodes.clear();

        String san = moveList.toSan();

        if (san == null || san.trim().isEmpty()) {
            return;
        }

        String[] moves = san.trim().split("\\s+");
        
        int moveIndex = 0;
        for (String moveToken : moves) {
            if (moveToken.isEmpty()) {
                continue;
            }

            if (Character.isDigit(moveToken.charAt(0))) {
                Text moveNumber = new Text(moveToken + " ");
                moveNumber.getStyleClass().add("move-number");
                moveListArea.getChildren().add(moveNumber);
            } else {
                Text move = new Text(moveToken + " ");
                final int index = moveIndex;
                move.setOnMouseClicked(event -> navigateToMove(index));
                move.getStyleClass().add("move-text");
                moveListArea.getChildren().add(move);
                moveTextNodes.add(move);
                moveIndex++;
            }
        }
        highlightCurrentMoveInTextFlow();
    }

    private void navigateToMove(int moveIndex) {
        if (moveList == null || moveIndex < -1 || moveIndex >= moveList.size()) {
            // Special case for going to start of game
            if (moveIndex == -1) {
                board.loadFromFen(moveList.getStartFen());
                currentMoveIndex = -1;
                updateBoardView();
                updateStockfishAnalysisIfRunning();
                updateNavigationButtonsState();
                highlightCurrentMoveInTextFlow();
            }
            return;
        }

        board.loadFromFen(moveList.getStartFen());
        for (int i = 0; i <= moveIndex; i++) {
            board.doMove(moveList.get(i));
        }
        currentMoveIndex = moveIndex;
        updateBoardView();
        updateStockfishAnalysisIfRunning();
        updateNavigationButtonsState();
        highlightCurrentMoveInTextFlow();
    }

    private void highlightCurrentMoveInTextFlow() {
        for (int i = 0; i < moveTextNodes.size(); i++) {
            Text moveText = moveTextNodes.get(i);
            moveText.getStyleClass().remove("current-move");
            if (i == currentMoveIndex) {
                moveText.getStyleClass().add("current-move");

                // Animation idea: A simple fade transition
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), moveText);
                ft.setFromValue(0.5);
                ft.setToValue(1.0);
                ft.play();
            }
        }
    }

    private void updateBoardView() {
        if (chessboard != null && board != null) {
            chessboard.setBoard(board);
        }
    }

    @FXML
    private void handleImportButtonAction() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showAlert("Input Error", "Username cannot be empty.", Alert.AlertType.WARNING);
            return;
        }
        importedGamesTable.getItems().clear();
        new Thread(() -> {
            try {
                List<ChessGame> games = chessComApiService.fetchAllGamesForUser(username);
                javafx.application.Platform.runLater(() -> {
                    if (games.isEmpty()) {
                        showAlert("No Games Found", "No games found for user: " + username, Alert.AlertType.INFORMATION);
                    } else {
                        ObservableList<ChessGame> observableGames = FXCollections.observableArrayList(games);
                        importedGamesTable.setItems(observableGames);
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        showAlert("Error", "Failed to fetch games: " + e.getMessage(), Alert.AlertType.ERROR));
                e.printStackTrace();
            }
        }).start();
    }

    private void handleSaveGame() {
        if (currentlyLoadedGame == null) {
            showAlert("Save Error", "No game is currently loaded.", Alert.AlertType.WARNING);
            saveStarButton.setSelected(false);
            return;
        }
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showAlert("Save Error", "Cannot save a game without a user context. Please fetch games for a user first.", Alert.AlertType.WARNING);
            saveStarButton.setSelected(false);
            return;
        }

        try {
            if (databaseService.isGameSaved(currentlyLoadedGame.getUrl())) {
                databaseService.deleteGame(currentlyLoadedGame.getUrl());
                showAlert("Game Removed", "Game removed from your collection.", Alert.AlertType.INFORMATION);
            } else {
                databaseService.saveGame(currentlyLoadedGame, username);
                showAlert("Success", "Game saved successfully to your collection.", Alert.AlertType.INFORMATION);
            }
            loadSavedGames();
            updateSaveStarState();

        } catch (SQLException e) {
            showAlert("Database Error", "An error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void handleDeleteGameFromCollection(ChessGame game) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Game");
        alert.setHeaderText("Are you sure you want to delete this game?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                databaseService.deleteGame(game.getUrl());
                showAlert("Game Deleted", "The game has been removed from your collection.", Alert.AlertType.INFORMATION);
                loadSavedGames();
                if (currentlyLoadedGame != null && currentlyLoadedGame.getUrl().equals(game.getUrl())) {
                    updateSaveStarState();
                }
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to delete the game: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }


    private void loadSavedGames() {
        try {
            List<ChessGame> savedGames = databaseService.getAllGames();
            ObservableList<ChessGame> observableGames = FXCollections.observableArrayList(savedGames);
            savedGamesTable.setItems(observableGames);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load saved games: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updateSaveStarState() {
        if (saveStarButton == null) return;
        if (currentlyLoadedGame == null) {
            saveStarButton.setSelected(false);
            return;
        }
        try {
            boolean isSaved = databaseService.isGameSaved(currentlyLoadedGame.getUrl());
            saveStarButton.setSelected(isSaved);
        } catch (SQLException e) {
            e.printStackTrace();
            saveStarButton.setSelected(false);
        }
    }

    private void analyzeCurrentPosition() {
        if (!isStockfishRunning.get() || board == null) {
            return;
        }

        depthLabel.setText("...");
        
        new Thread(() -> {
            String fen = board.getFen();
            List<AnalysisLine> lines = stockfishApiService.getAnalysisLines(fen);
            
            javafx.application.Platform.runLater(() -> {
                if (!lines.isEmpty()) {
                    depthLabel.setText("15"); // Assuming depth 15 as in API call
                    
                    ObservableList<AnalysisLine> observableLines = FXCollections.observableArrayList(lines);
                    engineMovesListView.setItems(observableLines);
                } else {
                    engineMovesListView.setItems(FXCollections.emptyObservableList());
                }
            });
        }).start();
    }

    private void clearAnalysis() {
        depthLabel.setText("-");
        if (engineMovesListView != null) {
            engineMovesListView.setItems(FXCollections.emptyObservableList());
        }
    }

    private void updateStockfishAnalysisIfRunning() {
        if (isStockfishRunning.get()) {
            analyzeCurrentPosition();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSettings() {
        System.out.println("Settings button clicked!");
    }

    private void handleFirstMove() {
        if (moveList == null || moveList.isEmpty()) return;
        navigateToMove(-1);
    }

    private void handlePrevMove() {
        if (moveList == null || currentMoveIndex < 0) return;
        navigateToMove(currentMoveIndex - 1);
    }

    private void handleNextMove() {
        if (moveList == null || currentMoveIndex >= moveList.size() - 1) return;
        navigateToMove(currentMoveIndex + 1);
    }

    private void handleLastMove() {
        if (moveList == null || moveList.isEmpty()) return;
        navigateToMove(moveList.size() - 1);
    }

    private void updateNavigationButtonsState() {
        boolean inGame = currentlyLoadedGame != null;
        if (!inGame) {
            firstMoveButton.setDisable(true);
            prevMoveButton.setDisable(true);
            nextMoveButton.setDisable(true);
            lastMoveButton.setDisable(true);
            return;
        }

        firstMoveButton.setDisable(currentMoveIndex < 0);
        prevMoveButton.setDisable(currentMoveIndex < 0);
        nextMoveButton.setDisable(currentMoveIndex >= moveList.size() - 1);
        lastMoveButton.setDisable(currentMoveIndex >= moveList.size() - 1);
    }

}
