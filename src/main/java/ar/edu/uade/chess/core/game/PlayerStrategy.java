package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.in.GameQueries;

/** How a player decides its move (Strategy): human input, AI, etc. */
public interface PlayerStrategy {
    /** The chosen move, or null if the player has no move to offer. */
    Move chooseMove(GameQueries game, Color color);

    /**
     * True if this player moves on its own (the computer). Undo and redo skip its turns,
     * so a human always gets the turn back.
     */
    default boolean isAutomatic() {
        return false;
    }
}
