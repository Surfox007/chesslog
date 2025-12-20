package com.chesslog;

import com.chesslog.api.ChessComApiService;
import com.chesslog.api.ChessGame;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.MoveList;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

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
    private TextArea analysisOutputArea;

    @FXML
    private TextArea moveListArea;

    @FXML private Button firstMoveButton;
    @FXML private Button prevMoveButton;
    @FXML private Button nextMoveButton;
    @FXML private Button lastMoveButton;

    @FXML private Button deleteButton;
    @FXML private Button saveButton;

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

    private final ChessComApiService chessComApiService = new ChessComApiService();

    private ChessGame currentlyLoadedGame;
    private Board board;
    private MoveList moveList;
    private int currentMoveIndex = -1;
    private Chessboard chessboard;

    @FXML
    public void initialize() {
        if (saveButton != null) {
            saveButton.setOnAction(e -> System.out.println("Save analysis clicked."));
        }
        if (deleteButton != null) {
            deleteButton.setOnAction(e -> System.out.println("Delete analysis clicked."));
        }
        if (importButton != null) {
            importButton.setOnAction(e -> handleImportButtonAction());
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
            boardContainer.getChildren().clear(); // Remove the placeholder
            boardContainer.getChildren().add(chessboard);
        }

        setupImportedGamesTable();
    }

    private void setupImportedGamesTable() {
        if (importedGamesTable == null) {
            return;
        }

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

    private void loadGame(ChessGame gameToLoad) {
        this.currentlyLoadedGame = gameToLoad;
        this.board = new Board();
        this.moveList = new MoveList();
        this.currentMoveIndex = -1;

        String pgn = currentlyLoadedGame.getPgn();
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

        // Clean the SAN string by removing the result at the end and other artifacts.
        sanMoves = sanMoves.replaceAll("\\{[^}]*\\}", ""); // Remove comments
        sanMoves = sanMoves.replace("1-0", "").replace("0-1", "").replace("1/2-1/2", "").replace("*", "").trim();

        try {
            moveList.loadFromSan(sanMoves);
            board.loadFromFen(moveList.getStartFen());
            moveListArea.setText(moveList.toSan());
            updateBoardView();

            if (mainTabPane != null) {
                mainTabPane.getSelectionModel().select(0);
            }

        } catch (Exception e) {
            showAlert("PGN Parse Error", "Failed to load moves from PGN: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updateBoardView() {
        if (chessboard != null && board != null) {
            chessboard.updateBoard(board);
        }
        System.out.println("Board FEN: " + board.getFen());
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

    @FXML
    private void handleFirstMove() {
        if (moveList == null || moveList.isEmpty()) return;
        board.loadFromFen(moveList.getStartFen());
        currentMoveIndex = -1;
        updateBoardView();
    }

    @FXML
    private void handlePrevMove() {
        if (moveList == null || currentMoveIndex < 0) return;
        board.undoMove();
        currentMoveIndex--;
        updateBoardView();
    }

    @FXML
    private void handleNextMove() {
        if (moveList == null || currentMoveIndex >= moveList.size() - 1) return;
        currentMoveIndex++;
        board.doMove(moveList.get(currentMoveIndex));
        updateBoardView();
    }

    @FXML
    private void handleLastMove() {
        if (moveList == null || moveList.isEmpty()) return;
        board.loadFromFen(moveList.getStartFen());
        for (com.github.bhlangonijr.chesslib.move.Move move : moveList) {
            board.doMove(move);
        }
        currentMoveIndex = moveList.size() - 1;
        updateBoardView();
    }
}
