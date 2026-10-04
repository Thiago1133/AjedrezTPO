package ar.edu.uade.chess.core.rules;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveHistory;

import java.util.List;

/**
 * A move that the pieces' own movement rules do not cover (castling, en passant,
 * promotion). New special rules are added by implementing this interface and
 * injecting them; existing code does not change.
 */
public interface SpecialMoveRule {
    boolean canApply(Board board, Move move, MoveHistory history);

    MoveCommand createCommand(Board board, Move move);

    /** Moves this rule could allow for the piece at from (used to list legal moves). */
    List<Move> getCandidateMoves(Board board, Position from, MoveHistory history);
}
