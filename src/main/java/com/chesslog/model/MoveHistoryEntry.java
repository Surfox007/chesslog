package com.chesslog.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MoveHistoryEntry {
    private final StringProperty moveNumber;
    private final StringProperty whiteMove;
    private final StringProperty blackMove;

    public MoveHistoryEntry(String moveNumber, String whiteMove, String blackMove) {
        this.moveNumber = new SimpleStringProperty(moveNumber);
        this.whiteMove = new SimpleStringProperty(whiteMove);
        this.blackMove = new SimpleStringProperty(blackMove);
    }

    public String getMoveNumber() {
        return moveNumber.get();
    }

    public StringProperty moveNumberProperty() {
        return moveNumber;
    }

    public String getWhiteMove() {
        return whiteMove.get();
    }

    public StringProperty whiteMoveProperty() {
        return whiteMove;
    }

    public String getBlackMove() {
        return blackMove.get();
    }

    public StringProperty blackMoveProperty() {
        return blackMove;
    }
}
