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
        Position skipped = skippedSquare(board, last);
        if (skipped == null || board.getPiece(last.getTo()).getColor() == piece.getColor()) {
            return false;
        }
        boolean victimBeside = last.getTo().getRow() == move.getFrom().getRow()
                && Math.abs(last.getTo().getColumn() - move.getFrom().getColumn()) == 1;
        return victimBeside && move.getTo().equals(skipped) && board.isEmpty(skipped);
    }

    /**
     * The square an EN_PASSANT piece skipped with the last move, if that move was its double
     * step; null otherwise. A capture en passant lands there.
     */
    public static Position skippedSquare(Board board, Move last) {
        if (last == null) {
            return null;
        }
        Piece moved = board.getPiece(last.getTo());
        int rows = last.getTo().getRow() - last.getFrom().getRow();
        boolean doubleStep = moved != null && moved.hasTrait(PieceTrait.EN_PASSANT)
                && last.getFrom().getColumn() == last.getTo().getColumn() && Math.abs(rows) == 2;
        return doubleStep ? new Position(last.getFrom().getRow() + rows / 2, last.getFrom().getColumn()) : null;
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
