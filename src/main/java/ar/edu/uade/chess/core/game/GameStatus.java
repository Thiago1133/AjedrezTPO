package ar.edu.uade.chess.core.game;

/**
 * Game phases. Kept as an enum (not the State pattern) on purpose: the set is
 * small and stable, and no phase has behavior of its own.
 */
public enum GameStatus {
    IN_PROGRESS(false),
    CHECK(false),
    CHECKMATE(true),
    STALEMATE(true),
    DRAW(true);

    private final boolean gameOver;

    GameStatus(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
