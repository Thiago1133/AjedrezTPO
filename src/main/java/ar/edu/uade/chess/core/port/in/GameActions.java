package ar.edu.uade.chess.core.port.in;

import ar.edu.uade.chess.core.board.Move;

/** Driving port, actions: what changes the game. Only the adapters that drive a game need it. */
public interface GameActions {
    void start();

    /** Asks the current player for a move and plays it. False if no legal move was made. */
    boolean playTurn();

    /** Plays the move for the current player. False if it is illegal or the game is over. */
    boolean move(Move move);

    /** Takes back the last move; against the computer, until the human is to move again. */
    void undo();

    /** Plays again what undo took back, also until the human is to move. */
    void redo();
}
