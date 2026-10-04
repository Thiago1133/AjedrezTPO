package ar.edu.uade.chess.core.command;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Move;

/**
 * A move as an object (Command pattern). It remembers what it changed during
 * execute so that undo restores the board exactly.
 */
public interface MoveCommand {
    void execute(Board board);

    void undo(Board board);

    Move getMove();

    /**
     * True if the position before this move can never occur again (a capture, a
     * pawn advance, castling...). Valid after execute. Used by draw rules.
     */
    boolean isIrreversible();
}
