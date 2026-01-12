package com.chesslog.model;

import com.google.gson.annotations.SerializedName;

public class ChessGame {

    @SerializedName("pgn")
    public String pgn;

    @SerializedName("url")
    public String url;

    // New fields for parsed PGN metadata
    public String event;
    public String site;
    public String date;
    public String whitePlayerName;
    public String blackPlayerName;
    public String result;
    
    @SerializedName("note")
    public String note;

    public String getPgn() {
        return pgn;
    }

    public String getUrl() {
        return url;
    }

    public String getEvent() {
        return event;
    }

    public String getSite() {
        return site;
    }

    public String getDate() {
        return date;
    }

    public String getWhitePlayerName() {
        return whitePlayerName;
    }

    public String getBlackPlayerName() {
        return blackPlayerName;
    }

    public String getResult() {
        return result;
    }
}
