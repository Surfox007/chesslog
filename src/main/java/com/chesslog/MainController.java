package com.chesslog;

import com.chesslog.api.ChessComApiService;
import com.chesslog.api.ChessGame;
import com.chesslog.api.Stockfish;
import com.chesslog.database.DatabaseService;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.MoveList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.sql.SQLException;
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
    private VBox analysisOutputArea;

    @FXML
    private Label evaluationLabel;

    @FXML
    private Label bestMoveLabel;

    @FXML
    private Label depthLabel;

    @FXML
    private VBox engineMovesVBox;

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
    private final Stockfish stockfish = new Stockfish();
    private final BooleanProperty isStockfishRunning = new SimpleBooleanProperty(false);
    private Thread stockfishAnalysisThread;

    private ChessGame currentlyLoadedGame;
    private Board board;
    private MoveList moveList;
    private int currentMoveIndex = -1;
    private Chessboard chessboard;

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

        // Initialize the chessboard and add it to the container
        chessboard = new Chessboard();
        if (boardContainer != null) {
            // The VBox contains the two HBoxes for player info.
            // We add the chessboard between them.
            boardContainer.getChildren().add(1, chessboard);
        }
        clearPlayerInfo();
        setupImportedGamesTable();
        setupSavedGamesTable();
        loadSavedGames();
        updateSaveStarState();

        if (stockfishToggle != null) {
            stockfishToggle.selectedProperty().bindBidirectional(isStockfishRunning);
            isStockfishRunning.addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    startStockfishAnalysis();
                } else {
                    stopStockfishAnalysis();
                }
            });
        }
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

            if (mainTabPane != null) {
                mainTabPane.getSelectionModel().select(0);
            }
            if (isStockfishRunning.get()) {
                // Restart analysis for new game
                stopStockfishAnalysis();
                startStockfishAnalysis();
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
        String[] moves = moveList.toSan().split(" ");
        int moveCounter = 1;
        boolean isWhiteMove = true;

        for (int i = 0; i < moves.length; i++) {
            if (Character.isDigit(moves[i].charAt(0))) {
                Text moveNumber = new Text(moves[i] + " ");
                moveNumber.getStyleClass().add("move-number");
                moveListArea.getChildren().add(moveNumber);
                isWhiteMove = true;
            } else {
                Text move = new Text(moves[i] + " ");
                moveListArea.getChildren().add(move);
                isWhiteMove = !isWhiteMove;
            }
        }
    }

    private void updateBoardView() {
        if (chessboard != null && board != null) {
            chessboard.updateBoard(board);
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
                // It's already saved, so "un-saving" it
                databaseService.deleteGame(currentlyLoadedGame.getUrl());
                showAlert("Game Removed", "Game removed from your collection.", Alert.AlertType.INFORMATION);
            } else {
                // It's not saved, so save it
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

    private void startStockfishAnalysis() {
        if (board == null) {
            isStockfishRunning.set(false);
            return;
        }

        stockfish.startEngine();
        stockfish.sendCommand("uci");
        stockfish.getOutput(100);
        stockfish.sendCommand("setoption name MultiPV value 10");

        stockfishAnalysisThread = new Thread(() -> {
            while (isStockfishRunning.get()) {
                try {
                    String fen = board.getFen();
                    stockfish.sendCommand("position fen " + fen);
                    stockfish.sendCommand("go depth 10");
                    String output = stockfish.getOutput(1000);

                    String bestMove = "N/A";
                    float evalScore = 0.0f;
                    int depth = 0;
                    final List<String> nextMoves = new java.util.ArrayList<>();

                    String[] lines = output.split("\n");
                    for (String line : lines) {
                        if (line.startsWith("info depth")) {
                            try {
                                if (line.contains("multipv 1")) {
                                    if (line.contains("score cp ")) {
                                        evalScore = Float.parseFloat(line.split("score cp ")[1].split(" ")[0]) / 100.0f;
                                    } else if (line.contains("score mate ")) {
                                        int mateIn = Integer.parseInt(line.split("score mate ")[1].split(" ")[0]);
                                        evalScore = (mateIn > 0) ? 100.0f : -100.0f;
                                    }
                                    bestMove = line.split(" pv ")[1].split(" ")[0];
                                }
                                if (line.contains(" pv ")) {
                                    nextMoves.add(line.split(" pv ")[1]);
                                }
                                depth = Integer.parseInt(line.split(" depth ")[1].split(" ")[0]);
                            } catch (Exception e) {
                                // Ignore parse errors
                            }
                        }
                    }

                    String finalBestMove = bestMove;
                    float finalEvalScore = evalScore;
                    int finalDepth = depth;

                    javafx.application.Platform.runLater(() -> {
                        bestMoveLabel.setText(finalBestMove);
                        evaluationLabel.setText(String.format("%.2f", finalEvalScore));
                        depthLabel.setText(String.valueOf(finalDepth));
                        engineMovesVBox.getChildren().clear();
                        for (String move : nextMoves) {
                            engineMovesVBox.getChildren().add(new Label(move));
                        }
                    });

                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> isStockfishRunning.set(false));
                }
            }
            stockfish.stopEngine();
        });
        stockfishAnalysisThread.setDaemon(true);
        stockfishAnalysisThread.start();
    }

    private void stopStockfishAnalysis() {
        isStockfishRunning.set(false);
        if (stockfishAnalysisThread != null) {
            stockfishAnalysisThread.interrupt();
            stockfishAnalysisThread = null;
        }
    }

    private void updateStockfishAnalysisIfRunning() {
        if (isStockfishRunning.get()) {
            stopStockfishAnalysis();
            startStockfishAnalysis();
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
        board.loadFromFen(moveList.getStartFen());
        currentMoveIndex = -1;
        updateBoardView();
        updateStockfishAnalysisIfRunning();
    }

    private void handlePrevMove() {
        if (moveList == null || currentMoveIndex < 0) return;
        board.undoMove();
        currentMoveIndex--;
        updateBoardView();
        updateStockfishAnalysisIfRunning();
    }

    private void handleNextMove() {
        if (moveList == null || currentMoveIndex >= moveList.size() - 1) return;
        currentMoveIndex++;
        board.doMove(moveList.get(currentMoveIndex));
        updateBoardView();
        updateStockfishAnalysisIfRunning();
    }

    private void handleLastMove() {
        if (moveList == null || moveList.isEmpty()) return;
        board.loadFromFen(moveList.getStartFen());
        for (com.github.bhlangonijr.chesslib.move.Move move : moveList) {
            board.doMove(move);
        }
        currentMoveIndex = moveList.size() - 1;
        updateBoardView();
        updateStockfishAnalysisIfRunning();
    }
}
