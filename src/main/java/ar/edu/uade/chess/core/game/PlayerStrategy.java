package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.ChessGame;

/** How a player decides its move (Strategy): human input, AI, etc. */
public interface PlayerStrategy {
    /** The chosen move, or null if the player has no move to offer. */
    Move chooseMove(ChessGame game, Color color);
}
