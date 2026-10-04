package ar.edu.uade.chess.core.rules;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.CastlingCommand;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceTrait;

import java.util.ArrayList;
import java.util.List;

/**
 * Castling: an unmoved royal piece with CASTLES moves two squares towards an
 * unmoved CASTLES partner on the same row, which jumps to the square it crossed.
 * The path must be empty, and the king may not be in check, cross an attacked
 * square or land on one. Works on any board width.
 */
public class CastlingRule implements SpecialMoveRule {
    private static final int KING_STEPS = 2;

    private final CheckDetector checkDetector;

    public CastlingRule(CheckDetector checkDetector) {
        this.checkDetector = checkDetector;
    }

    @Override
    public boolean canApply(Board board, Move move, MoveHistory history) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece king = board.getPiece(from);
        if (king == null || !king.hasTrait(PieceTrait.ROYAL) || !king.hasTrait(PieceTrait.CASTLES) || king.hasMoved()
                || from.getRow() != to.getRow() || Math.abs(to.getColumn() - from.getColumn()) != KING_STEPS
                || !board.isInside(to)) {
            return false;
        }
        Direction step = new Direction(0, Integer.signum(to.getColumn() - from.getColumn()));
        Position rookPosition = findPartner(board, from, step);
        if (rookPosition == null) {
            return false;
        }
        Position crossed = from.offset(step);
        boolean rookBeyondKingTarget = Math.abs(rookPosition.getColumn() - from.getColumn()) > KING_STEPS;
        return rookBeyondKingTarget
                && !checkDetector.isInCheck(board, king.getColor())
                && !checkDetector.isSquareAttacked(board, crossed, king.getColor().opposite())
                && !checkDetector.isSquareAttacked(board, to, king.getColor().opposite());
    }

    @Override
    public MoveCommand createCommand(Board board, Move move) {
        Direction step = new Direction(0, Integer.signum(move.getTo().getColumn() - move.getFrom().getColumn()));
        Position rookPosition = findPartner(board, move.getFrom(), step);
        return new CastlingCommand(move, new Move(rookPosition, move.getFrom().offset(step)));
    }

    @Override
    public List<Move> getCandidateMoves(Board board, Position from, MoveHistory history) {
        List<Move> result = new ArrayList<>();
        for (int side : new int[] {-KING_STEPS, KING_STEPS}) {
            Move candidate = new Move(from, new Position(from.getRow(), from.getColumn() + side));
            if (canApply(board, candidate, history)) {
                result.add(candidate);
            }
        }
        return result;
    }

    /** First piece along the row; returns it only if it is a valid unmoved castling partner. */
    private Position findPartner(Board board, Position kingPosition, Direction step) {
        Piece king = board.getPiece(kingPosition);
        for (Position current = kingPosition.offset(step); board.isInside(current); current = current.offset(step)) {
            Piece piece = board.getPiece(current);
            if (piece != null) {
                boolean partner = piece.getColor() == king.getColor() && piece.hasTrait(PieceTrait.CASTLES)
                        && !piece.hasTrait(PieceTrait.ROYAL) && !piece.hasMoved();
                return partner ? current : null;
            }
        }
        return null;
    }
}
