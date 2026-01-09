package com.chesslog;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class Chessboard extends GridPane {

    private static final String LIGHT_SQUARE_COLOR = "#F0D9B5";
    private static final String DARK_SQUARE_COLOR = "#B58863";
    private static final String HIGHLIGHT_COLOR = "#7B61FF";
    private static final String LEGAL_MOVE_HIGHLIGHT_COLOR = "#A28DFF";
    private static final int SQUARE_SIZE = 75;

    private Board board;
    private BiConsumer<Square, Square> onMoveAttempted;
    private Square selectedSquare = null;

    private final StackPane[][] squarePanes = new StackPane[8][8];
    private final List<Circle> legalMoveMarkers = new ArrayList<>();

    public Chessboard() {
        setupGrid();
    }

    public void setBoard(Board board) {
        this.board = board;
        updateBoard();
    }

    public void setOnMoveAttempted(BiConsumer<Square, Square> onMoveAttempted) {
        this.onMoveAttempted = onMoveAttempted;
    }

    private void setupGrid() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane squarePane = new StackPane();
                Rectangle background = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
                background.setFill((row + col) % 2 == 0 ? Color.web(LIGHT_SQUARE_COLOR) : Color.web(DARK_SQUARE_COLOR));
                squarePane.getChildren().add(background);
                squarePanes[row][col] = squarePane;

                final int r = row;
                final int c = col;

                squarePane.setOnMouseClicked(event -> {
                    // This maps the visual row/col to the chess Rank and File
                    Rank rank = Rank.fromValue("RANK_" + (8 - r));
                    File file = File.fromValue("FILE_" + (char)('A' + c));
                    Square clickedSquare = Square.encode(rank, file);
                    handleSquareClick(clickedSquare);
                });

                this.add(squarePane, col, row);
            }
        }
    }

    private void handleSquareClick(Square clickedSquare) {
        if (board == null) return;

        // If a piece was already selected
        if (selectedSquare != null) {
            // If the user clicked a different square, attempt a move
            if (selectedSquare != clickedSquare) {
                if (onMoveAttempted != null) {
                    onMoveAttempted.accept(selectedSquare, clickedSquare);
                }
            }
            // In any case of a second click (same square or different), clear selection
            clearHighlights();
            selectedSquare = null;
        }
        // If no piece was selected, try to select one
        else {
            Piece piece = board.getPiece(clickedSquare);
            // Only select if it's a valid piece of the current player's side
            if (piece != Piece.NONE && piece.getPieceSide() == board.getSideToMove()) {
                selectedSquare = clickedSquare;
                highlightSquare(clickedSquare, true);
                highlightLegalMoves(clickedSquare);
            }
        }
    }

    private void highlightSquare(Square square, boolean highlight) {
        int row = 7 - square.getRank().ordinal();
        int col = square.getFile().ordinal();
        StackPane pane = squarePanes[row][col];
        Rectangle rect = (Rectangle) pane.getChildren().get(0);

        if (highlight) {
            rect.setStroke(Color.web(HIGHLIGHT_COLOR));
            rect.setStrokeWidth(4);
        } else {
            rect.setStroke(null);
        }
    }

    private void highlightLegalMoves(Square from) {
        List<Move> legalMoves = board.legalMoves();
        for (Move move : legalMoves) {
            if (move.getFrom() == from) {
                int row = 7 - move.getTo().getRank().ordinal();
                int col = move.getTo().getFile().ordinal();
                StackPane pane = squarePanes[row][col];

                Circle marker = new Circle(SQUARE_SIZE / 4);
                marker.setFill(Color.web(LEGAL_MOVE_HIGHLIGHT_COLOR, 0.7));
                marker.setMouseTransparent(true);
                pane.getChildren().add(marker);
                legalMoveMarkers.add(marker);
            }
        }
    }

    private void clearHighlights() {
        if (selectedSquare != null) {
            highlightSquare(selectedSquare, false);
        }
        for (Circle marker : legalMoveMarkers) {
            ((StackPane)marker.getParent()).getChildren().remove(marker);
        }
        legalMoveMarkers.clear();
    }

    public void updateBoard() {
        if (board == null) return;

        this.getChildren().removeIf(node -> node instanceof Label);
        clearHighlights();

        for (Square square : Square.values()) {
            if (square == Square.NONE) continue;

            Piece piece = board.getPiece(square);
            if (piece != Piece.NONE) {
                Label pieceLabel = new Label(getUnicodePiece(piece));
                pieceLabel.setStyle("-fx-font-size: 48px;");
                pieceLabel.setMouseTransparent(true);

                int displayRow = 7 - square.getRank().ordinal();
                int displayCol = square.getFile().ordinal();

                GridPane.setHalignment(pieceLabel, HPos.CENTER);
                GridPane.setValignment(pieceLabel, VPos.CENTER);
                this.add(pieceLabel, displayCol, displayRow);
            }
        }
    }

    private String getUnicodePiece(Piece piece) {
        switch (piece) {
            case WHITE_PAWN: return "♙";
            case WHITE_KNIGHT: return "♘";
            case WHITE_BISHOP: return "♗";
            case WHITE_ROOK: return "♖";
            case WHITE_QUEEN: return "♕";
            case WHITE_KING: return "♔";
            case BLACK_PAWN: return "♟";
            case BLACK_KNIGHT: return "♞";
            case BLACK_BISHOP: return "♝";
            case BLACK_ROOK: return "♜";
            case BLACK_QUEEN: return "♛";
            case BLACK_KING: return "♚";
            default: return "";
        }
    }
}
