package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Color;

/** Keeps track of whose turn it is. White always starts. */
public class TurnManager {
    private final Player white;
    private final Player black;
    private Player current;

    public TurnManager(Player white, Player black) {
        if (white.getColor() != Color.WHITE || black.getColor() != Color.BLACK) {
            throw new IllegalArgumentException("Players must be white and black, in that order");
        }
        this.white = white;
        this.black = black;
        this.current = white;
    }

    public Player getCurrentPlayer() {
        return current;
    }

    public Color getCurrentColor() {
        return current.getColor();
    }

    public void next() {
        current = current == white ? black : white;
    }

    /** With two players going back one turn is the same as going forward. */
    public void previous() {
        next();
    }
}
