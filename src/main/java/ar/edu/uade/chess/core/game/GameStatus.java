package ar.edu.uade.chess.core.game;

/**
 * Game phases. Kept as an enum (not the State pattern) on purpose: the set is
 * small and stable, and no phase has behavior of its own.
 */
public enum GameStatus {
    IN_PROGRESS(false, false),
    CHECK(false, false),
    CHECKMATE(true, false),
    STALEMATE(true, false),
    DRAW(true, false),
    WHITE_TIMEOUT(true, true),
    BLACK_TIMEOUT(true, true);

    private final boolean gameOver;
    private final boolean definitive;

    GameStatus(boolean gameOver, boolean definitive) {
        this.gameOver = gameOver;
        this.definitive = definitive;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * True if the result cannot be taken back: a loss on time ends the game for good, as
     * with a real chess clock. A position-based result (checkmate, draw) can still be undone.
     */
    public boolean isDefinitive() {
        return definitive;
    }
}
