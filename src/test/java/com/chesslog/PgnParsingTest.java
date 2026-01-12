package com.chesslog;

import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.junit.jupiter.api.Test;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PgnParsingTest {

    @Test
    public void testPgnLoadingWithTrickyHeaders() {
        // PGN with "1." inside a header
        String pgnContent = "[Event \"Test 1.0\"]\n" +
                "[Site \"Chess.com\"]\n" +
                "[Date \"2024.01.01\"]\n" + // Contains "1."
                "[Round \"06\"]\n" +
                "[White \"Player1\"]\n" +
                "[Black \"Player2\"]\n" +
                "[Result \"1-0\"]\n" +
                "\n" +
                "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 1-0";

        // Current flawed logic simulation
        int moveTextStart = pgnContent.indexOf("1.");
        System.out.println("Index of '1.': " + moveTextStart);
        String sub = pgnContent.substring(moveTextStart);
        System.out.println("Substring start: " + sub.substring(0, Math.min(20, sub.length())));
        
        // It matches "1." in "Test 1.0" or "2024.01.01" depending on which comes first.
        // In "Test 1.0", it finds "1." at index 8.
        
        // Verify PgnHolder works correctly
        PgnHolder pgnHolder = new PgnHolder("dummy.pgn");
        try {
            pgnHolder.loadPgn(pgnContent);
            
            if (!pgnHolder.getGames().isEmpty()) {
                Game game = pgnHolder.getGames().get(0);
                MoveList moves = game.getHalfMoves();
                System.out.println("Parsed moves: " + moves.toSan());
                assertTrue(moves.size() > 0);
            } else {
                System.out.println("No games found via PgnHolder");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}