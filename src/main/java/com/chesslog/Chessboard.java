package com.chesslog;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Chessboard extends GridPane {

    private static final String LIGHT_SQUARE_COLOR = "#F0D9B5";
    private static final String DARK_SQUARE_COLOR = "#B58863";
    private static final int SQUARE_SIZE = 75;

    public Chessboard() {
        setupGrid();
    }

    private void setupGrid() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = new StackPane();
                Rectangle background = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
                background.setFill((row + col) % 2 == 0 ? Color.web(LIGHT_SQUARE_COLOR) : Color.web(DARK_SQUARE_COLOR));
                square.getChildren().add(background);
                this.add(square, col, row);
            }
        }
    }

    public void updateBoard(Board board) {
        // Clear old pieces
        this.getChildren().removeIf(node -> node instanceof Label);

        for (Square square : Square.values()) {
            if (square == Square.NONE) continue;

            Piece piece = board.getPiece(square);
            if (piece != Piece.NONE) {
                Label pieceLabel = new Label(getUnicodePiece(piece));
                pieceLabel.setStyle("-fx-font-size: 48px;");

                // The board in chesslib is indexed from A1=(0,0) upwards, but JavaFX GridPane is (col, row) from top-left.
                // We need to invert the row index.
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
