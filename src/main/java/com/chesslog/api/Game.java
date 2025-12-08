package com.chesslog.api;

import com.google.gson.annotations.SerializedName;

public class Game {

    @SerializedName("pgn")
    public String pgn;

    @SerializedName("url")
    public String url;

    public String getPgn() {
        return pgn;
    }

    public String getUrl() {
        return url;
    }
}