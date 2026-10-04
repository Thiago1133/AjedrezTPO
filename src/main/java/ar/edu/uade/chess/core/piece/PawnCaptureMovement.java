package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.board.Position;

import java.util.ArrayList;
import java.util.List;

/** Moves one square diagonally forward, and only to capture an enemy piece. */
public class PawnCaptureMovement implements MovementRule {

    @Override
    public boolean canMove(Piece piece, Position from, Position to, Board board) {
        return getReachablePositions(piece, from, board).contains(to);
    }

    @Override
    public List<Position> getReachablePositions(Piece piece, Position from, Board board) {
        List<Position> result = new ArrayList<>();
        int forward = piece.getColor().forward();
        for (int side : new int[] {-1, 1}) {
            Position target = from.offset(new Direction(forward, side));
            if (!board.isInside(target)) {
                continue;
            }
            Piece occupant = board.getPiece(target);
            if (occupant != null && occupant.getColor() != piece.getColor()) {
                result.add(target);
            }
        }
        return result;
    }
}
