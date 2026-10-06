package ar.edu.uade.chess.core.rules;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.EnPassantCommand;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceTrait;

import java.util.ArrayList;
import java.util.List;

/**
 * En passant: right after an enemy EN_PASSANT piece advances two squares and lands
 * beside one of ours, ours may capture it by moving diagonally to the square it skipped.
 */
public class EnPassantRule implements SpecialMoveRule {

    @Override
    public boolean canApply(Board board, Move move, MoveHistory history) {
        Piece piece = board.getPiece(move.getFrom());
        Move last = history.getLastMove();
        if (piece == null || !piece.hasTrait(PieceTrait.EN_PASSANT) || last == null) {
            return false;
        }
        Piece victim = board.getPiece(last.getTo());
        boolean victimJustDoubleStepped = victim != null
                && victim.getColor() != piece.getColor()
                && victim.hasTrait(PieceTrait.EN_PASSANT)
                && last.getFrom().getColumn() == last.getTo().getColumn()
                && Math.abs(last.getTo().getRow() - last.getFrom().getRow()) == 2;
        if (!victimJustDoubleStepped) {
            return false;
        }
        boolean victimBeside = last.getTo().getRow() == move.getFrom().getRow()
                && Math.abs(last.getTo().getColumn() - move.getFrom().getColumn()) == 1;
        Position skipped = new Position(move.getFrom().getRow() + piece.getColor().forward(), last.getTo().getColumn());
        return victimBeside && move.getTo().equals(skipped) && board.isEmpty(skipped);
    }

    @Override
    public MoveCommand createCommand(Board board, Move move) {
        return new EnPassantCommand(move, new Position(move.getFrom().getRow(), move.getTo().getColumn()));
    }

    @Override
    public List<Move> getCandidateMoves(Board board, Position from, MoveHistory history) {
        List<Move> result = new ArrayList<>();
        Piece piece = board.getPiece(from);
        if (piece == null) {
            return result;
        }
        for (int side : new int[] {-1, 1}) {
            Move candidate = new Move(from,
                    new Position(from.getRow() + piece.getColor().forward(), from.getColumn() + side));
            if (board.isInside(candidate.getTo()) && canApply(board, candidate, history)) {
                result.add(candidate);
            }
        }
        return result;
    }
}
