package ar.edu.uade.chess.core.port.out;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;

/** Driven port: where a human player's moves come from (console, GUI, network...). */
public interface MoveInput {
    /** The chosen move, or null if the player wants to stop. */
    Move readMove(Color color);
}
