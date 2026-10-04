package ar.edu.uade.chess.core.command;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Move;

/**
 * A move as an object (Command pattern). It remembers what it changed during
 * execute so that undo restores the board exactly. An instance is bound to the
 * board it was executed on.
 */
public interface MoveCommand {
    void execute(Board board);

    void undo(Board board);

    Move getMove();
}
