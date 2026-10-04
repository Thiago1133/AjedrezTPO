package ar.edu.uade.chess.core.port;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.game.GameStatus;

import java.util.List;

/**
 * Driving port: everything an adapter (console, GUI, web...) may ask the core.
 * Adapters depend on this interface, never on the concrete Game.
 */
public interface ChessGame {
    void start();

    /** Asks the current player for a move and plays it. False if no legal move was made. */
    boolean playTurn();

    /** Plays the move for the current player. False if it is illegal or the game is over. */
    boolean move(Move move);

    void undo();

    void redo();

    /** A copy of the board: adapters can read it but cannot alter the game through it. */
    Board getBoard();

    Color getCurrentTurn();

    /** Every legal move for the player to move (empty if the game is over). */
    List<Move> getLegalMoves();

    GameStatus getStatus();

    void addObserver(GameObserver observer);

    void removeObserver(GameObserver observer);
}
