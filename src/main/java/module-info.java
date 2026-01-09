module com.chesslog.desktop {
    // JavaFX requirements
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.sql;
    requires okhttp3;


    // ChessLib dependency (if confirmed to be working)
    requires chesslib;

    // Open packages for reflection access
    opens com.chesslog to javafx.fxml;


    // Open packages to the Gson module for serialization/deserialization
    opens com.chesslog.model to com.google.gson;
    opens com.chesslog.service to com.google.gson;

    exports com.chesslog;
    exports com.chesslog.model;
    exports com.chesslog.service;
}
