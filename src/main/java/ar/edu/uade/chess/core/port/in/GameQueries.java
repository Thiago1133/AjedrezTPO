package ar.edu.uade.chess.core.port.in;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.game.GameStatus;
import ar.edu.uade.chess.core.piece.Piece;

import java.util.List;

/**
 * Driving port, read-only: everything about the game that can be asked without changing it.
 * A player strategy (the computer) receives only this, so it can look but never move or undo.
 */
public interface GameQueries {
    /** A copy of the board: it can be read but the game cannot be altered through it. */
    Board getBoard();

    Color getCurrentTurn();

    GameStatus getStatus();

    /** Every legal move for the player to move (empty if the game is over). */
    List<Move> getLegalMoves();

    /** True if the move is legal now and takes a piece (including en passant). */
    boolean isCapture(Move move);

    /** True if the move is legal now and promotes, so the player should choose the new piece. */
    boolean isPromotion(Move move);

    /** The last move played (after undo, the one before it), or null at the start. */
    Move getLastMove();

    /** Pieces the given side has captured, most valuable first. */
    List<Piece> getCapturedPieces(Color capturer);

    /** Value of captured material the given side is ahead by (0 if it is not ahead). */
    int getMaterialAdvantage(Color color);

    /** True if there is a move to take back. */
    boolean canUndo();

    /** True if there is an undone move to play again (a new move discards them). */
    boolean canRedo();
}
