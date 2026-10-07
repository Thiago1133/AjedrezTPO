package ar.edu.uade.chess.core.rules;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.command.PromotionCommand;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.PieceTrait;
import ar.edu.uade.chess.core.port.PromotionOptions;

import java.util.Comparator;
import java.util.List;

/**
 * Promotion: a PROMOTES piece that reaches the last row (in its forward direction,
 * on any board size) is replaced by the chosen piece. A missing or invalid choice
 * (unknown, royal or another promoting piece) becomes the most valuable valid choice.
 * The choices are every registered piece that is neither royal nor promoting, so a new
 * piece can be promoted to as soon as it is registered in the factory.
 */
public class PromotionRule implements SpecialMoveRule, PromotionOptions {
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

    @Override
    public List<Piece> getPromotionChoices(Color color) {
        return pieceFactory.createAll(color).stream()
                .filter(PromotionRule::isValidChoice)
                .sorted(Comparator.comparingInt(Piece::getValue).reversed())
                .toList();
    }

    private static boolean isValidChoice(Piece piece) {
        return !piece.hasTrait(PieceTrait.ROYAL) && !piece.hasTrait(PieceTrait.PROMOTES);
    }

    private Piece createPromotedPiece(String requestedId, Piece pawn) {
        if (requestedId != null) {
            try {
                Piece chosen = pieceFactory.create(requestedId, pawn.getColor());
                if (isValidChoice(chosen)) {
                    return chosen;
                }
            } catch (IllegalArgumentException unknownId) {
                // falls through to the default piece
            }
        }
        List<Piece> choices = getPromotionChoices(pawn.getColor());
        if (choices.isEmpty()) {
            throw new IllegalStateException("No piece registered to promote to");
        }
        return choices.get(0);
    }
}
