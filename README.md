# ChessLog Desktop

A JavaFX-based desktop application for chess enthusiasts to analyze, collect, and manage their chess games. ChessLog allows you to import games from Chess.com, analyze them with the Stockfish engine, take notes, and build a personal database of your favorite games and variations.

## Features

### ♟️ Game Analysis & Board
-   **Interactive Chessboard:** Make moves, flip the board, and navigate through the game history.
-   **Stockfish Integration:** Analyze positions with the built-in Stockfish chess engine to see the best moves and evaluation scores.
-   **Branching Variations:** Make new moves in the middle of a loaded game to explore "what-if" scenarios. The application automatically handles branching.
-   **Analysis Notes:** Write and save personal notes for any game or position directly within the analysis tab.

### 📚 Collection Management
-   **Local Database:** All your games are saved locally in a SQLite database (`chesslog.db`), ensuring your data is private and offline-accessible.
-   **Save & Rename:** Save your analysis with custom Event names and player details.
-   **Smart Saving:**
    -   **New Variations:** If you modify moves, saving creates a new entry, preserving the original game.
    -   **Renaming:** If you only edit names or notes, the existing entry is updated.
-   **Collection Browser:** View your saved games in a searchable table.
-   **Delete:** Remove games you no longer need from your collection.

### 🌐 Import
-   **Chess.com Import:** Fetch recent games for any Chess.com user directly into the application.

## Getting Started

### Prerequisites
-   **Java JDK 21** or later.
-   **Maven** (optional, wrapper provided).

### Installation & Running

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/yourusername/chesslog.git
    cd chesslog
    ```

2.  **Build and Run (Windows):**
    ```powershell
    ./mvnw clean javafx:run
    ```

3.  **Build and Run (Linux/macOS):**
    ```bash
    ./mvnw clean javafx:run
    ```

## Usage Guide

1.  **Start Analysis:** Click the **"+"** button in the *Games > Your Collections* tab to start a fresh analysis board.
2.  **Import Games:** Go to *Games > Import*, enter a Chess.com username, and click "Fetch Games". Click "Load" on any game to analyze it.
3.  **Analyze:**
    -   Toggle the **Engine** switch to see Stockfish evaluations.
    -   Use the navigation buttons (`<`, `>`, `<<`, `>>`) to move through the game.
    -   Make moves on the board to explore variations.
4.  **Save:**
    -   Click the **Star (☆)** button to save.
    -   A dialog will appear allowing you to set the Event Name, Player Names, and add Notes.
    -   Click "Save" to store it in your collection.

## Project Structure

-   `src/main/java/com/chesslog`: Source code.
    -   `model`: Data classes (`ChessGame`, `AnalysisLine`).
    -   `service`: Handlers for Database, Chess.com API, and Stockfish.
    -   `MainController.java`: The core logic connecting the UI to the backend.
-   `src/main/resources`: FXML layout files and assets.
-   `chesslog.db`: The local SQLite database file (created upon first run).

## Technologies Used

-   **JavaFX:** For the User Interface.
-   **SQLite (JDBC):** For local data persistence.
-   **ChessLib:** For chess logic, move generation, and PGN parsing.
-   **Stockfish:** Open-source chess engine for analysis.
-   **Maven:** Project management and build tool.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

[MIT License](LICENSE)
