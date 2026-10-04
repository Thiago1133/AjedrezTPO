package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceTrait;
import ar.edu.uade.chess.core.rules.CheckDetector;

/**
 * One-move material evaluation: value captured, plus a promotion bonus, minus the
 * value of the moved piece if it lands on an attacked square. It does not look
 * further ahead; deeper evaluators can be plugged in instead.
 */
public class MaterialEvaluationStrategy implements MoveEvaluationStrategy {
    /** Approximate gain of turning a pawn into a queen. */
    private static final int PROMOTION_BONUS = 8;

    private final CheckDetector checkDetector;

    public MaterialEvaluationStrategy(CheckDetector checkDetector) {
        this.checkDetector = checkDetector;
    }

    @Override
    public int evaluate(Board board, Move move, Color color) {
        Board after = board.copy();
        Piece mover = after.getPiece(move.getFrom());
        Position capturedAt = capturedSquare(after, move, mover);
        Piece captured = after.removePiece(capturedAt);
        int score = captured != null && captured.getColor() != color ? captured.getValue() : 0;
        after.movePiece(move.getFrom(), move.getTo());

        int moverValue = mover.getValue();
        if (promotes(after, move, mover)) {
            score += PROMOTION_BONUS;
            moverValue += PROMOTION_BONUS;
        }
        if (!mover.hasTrait(PieceTrait.ROYAL) && checkDetector.isSquareAttacked(after, move.getTo(), color.opposite())) {
            score -= moverValue;
        }
        return score;
    }

    /** The destination, except for an en passant capture where the victim is beside the origin. */
    private Position capturedSquare(Board board, Move move, Piece mover) {
        boolean diagonalToEmpty = board.isEmpty(move.getTo()) && move.getFrom().getColumn() != move.getTo().getColumn();
        if (mover.hasTrait(PieceTrait.EN_PASSANT) && diagonalToEmpty) {
            return new Position(move.getFrom().getRow(), move.getTo().getColumn());
        }
        return move.getTo();
    }

    private boolean promotes(Board board, Move move, Piece mover) {
        Position beyond = move.getTo().offset(new Direction(mover.getColor().forward(), 0));
        return mover.hasTrait(PieceTrait.PROMOTES) && !board.isInside(beyond);
    }
}
