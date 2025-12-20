package com.chesslog.api;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class MonthlyArchive {

    @SerializedName("games")
    public List<ChessGame> games;

    public List<ChessGame> getGames() {
        return games;
    }
}