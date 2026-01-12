package com.chesslog;

import com.github.bhlangonijr.chesslib.move.MoveList;

public class SanTest {
    public static void main(String[] args) {
        MoveList moves = new MoveList();
        try {
            moves.loadFromSan("1. e4 e5 2. Nf3 Nc6");
            System.out.println("Output: [" + moves.toSan() + "]");
            
            // Check split behavior
            String[] tokens = moves.toSan().trim().split("\\s+");
            for (String t : tokens) {
                System.out.println("Token: '" + t + "'");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

