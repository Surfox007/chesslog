module com.chesslog.desktop {
    // JavaFX requirements
    requires transitive javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires okhttp3;
    requires com.google.gson;

    // ChessLib dependency (if confirmed to be working)
    // requires chesslib;

    // Open packages for reflection access
    opens com.chesslog to javafx.fxml;


    // Open your API package to the Gson module
    opens com.chesslog.api to com.google.gson;

    exports com.chesslog;
}