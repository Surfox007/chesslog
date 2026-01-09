package com.chesslog.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class UserArchive {

    @SerializedName("archives")
    public List<String> archiveUrls;

    public List<String> getArchiveUrls() {
        return archiveUrls;
    }
}
