package com.chesslog.model;

/**
 * Represents a single analysis line from a chess engine.
 */
public class AnalysisLine {
    private final String evaluation;
    private final String bestMove;
    private final String continuation;
    private final boolean isMate;

    public AnalysisLine(String evaluation, String bestMove, String continuation, boolean isMate) {
        this.evaluation = evaluation;
        this.bestMove = bestMove;
        this.continuation = continuation;
        this.isMate = isMate;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public String getBestMove() {
        return bestMove;
    }

    public String getContinuation() {
        return continuation;
    }

    public boolean isMate() {
        return isMate;
    }

    /**
     * Formats the evaluation string for display.
     */
    public String getFormattedEvaluation() {
        if (isMate) {
            return "M" + evaluation;
        }
        try {
            double eval = Double.parseDouble(evaluation);
            return (eval > 0 ? "+" : "") + String.format("%.2f", eval);
        } catch (NumberFormatException e) {
            return evaluation;
        }
    }
}
