package ar.edu.uade.chess.core.rules;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.command.PromotionCommand;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.PieceTrait;

import java.util.List;

/**
 * Promotion: a PROMOTES piece that reaches the last row (in its forward direction,
 * on any board size) is replaced by the chosen piece. A missing or invalid choice
 * (unknown, royal or another promoting piece) becomes a queen.
 */
public class PromotionRule implements SpecialMoveRule {
    private static final String DEFAULT_PROMOTION = "queen";

    private final PieceFactory pieceFactory;

    public PromotionRule(PieceFactory pieceFactory) {
        this.pieceFactory = pieceFactory;
    }

    @Override
    public boolean canApply(Board board, Move move, MoveHistory history) {
        Piece piece = board.getPiece(move.getFrom());
        if (piece == null || !piece.hasTrait(PieceTrait.PROMOTES)) {
            return false;
        }
        Position beyond = move.getTo().offset(new Direction(piece.getColor().forward(), 0));
        return board.isInside(move.getTo()) && !board.isInside(beyond)
                && piece.canMove(move.getFrom(), move.getTo(), board);
    }

    @Override
    public MoveCommand createCommand(Board board, Move move) {
        Piece pawn = board.getPiece(move.getFrom());
        return new PromotionCommand(move, createPromotedPiece(move.getPromotionId(), pawn));
    }

    /** Regular moves to the last row are already listed; they promote to the default piece. */
    @Override
    public List<Move> getCandidateMoves(Board board, Position from, MoveHistory history) {
        return List.of();
    }

    private Piece createPromotedPiece(String requestedId, Piece pawn) {
        if (requestedId != null) {
            try {
                Piece chosen = pieceFactory.create(requestedId, pawn.getColor());
                if (!chosen.hasTrait(PieceTrait.ROYAL) && !chosen.hasTrait(PieceTrait.PROMOTES)) {
                    return chosen;
                }
            } catch (IllegalArgumentException unknownId) {
                // falls through to the default piece
            }
        }
        return pieceFactory.create(DEFAULT_PROMOTION, pawn.getColor());
    }
}
