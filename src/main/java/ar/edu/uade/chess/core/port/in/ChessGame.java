package ar.edu.uade.chess.core.port.in;

import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.out.GameObserver;

/**
 * The driving port for adapters that run a game (window, console): actions and subscriptions,
 * plus every query. Clients that only read the game, like the computer player, depend on
 * GameQueries alone. Adapters depend on these interfaces, never on the concrete Game.
 */
public interface ChessGame extends GameQueries {
    void start();

    /** Asks the current player for a move and plays it. False if no legal move was made. */
    boolean playTurn();

    /** Plays the move for the current player. False if it is illegal or the game is over. */
    boolean move(Move move);

    /** Advances the active player's clock, if this game has a finite clock in progress. */
    void advanceClock(int seconds);

    /** Takes back the last move; against the computer, until the human is to move again. */
    void undo();

    /** Plays again what undo took back, also until the human is to move. */
    void redo();

    /** Who is told what happens. Whoever subscribes must also unsubscribe. */
    void addObserver(GameObserver observer);

    void removeObserver(GameObserver observer);
}
