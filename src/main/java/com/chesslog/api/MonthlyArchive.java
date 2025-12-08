package com.chesslog.api;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class MonthlyArchive {

    @SerializedName("games")
    public List<Game> games;

    public List<Game> getGames() {
        return games;
    }
}