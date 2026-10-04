package ar.edu.uade.chess.core.rules;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.JumpMovement;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceTrait;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Answers whether royal pieces are in check. Works for any board size and any royal piece. */
public class CheckDetector {

    public boolean isInCheck(Board board, Color color) {
        for (Position royal : findRoyalPieces(board, color)) {
            if (isSquareAttacked(board, royal, color.opposite())) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if some attacker piece could capture on the position. Empty squares are
     * probed with a dummy defender so that capture-only moves (pawns) are counted.
     */
    public boolean isSquareAttacked(Board board, Position position, Color attacker) {
        Board target = board;
        Piece occupant = board.getPiece(position);
        if (occupant == null || occupant.getColor() == attacker) {
            target = board.copy();
            target.placePiece(probe(attacker.opposite()), position);
        }
        for (Position from : target.getPositionsOf(attacker)) {
            if (target.getPiece(from).canMove(from, position, target)) {
                return true;
            }
        }
        return false;
    }

    public List<Position> findRoyalPieces(Board board, Color color) {
        List<Position> result = new ArrayList<>();
        for (Position position : board.getPositionsOf(color)) {
            if (board.getPiece(position).hasTrait(PieceTrait.ROYAL)) {
                result.add(position);
            }
        }
        return result;
    }

    private static Piece probe(Color color) {
        return new Piece("probe", '?', color, 0, Set.of(), List.of(new JumpMovement(List.of())));
    }
}
