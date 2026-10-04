package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.board.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Moves along each direction until blocked, up to maxSteps squares.
 * Can capture the first enemy piece it meets. Covers rook, bishop, queen and king.
 */
public class SlidingMovement implements MovementRule {
    private final List<Direction> directions;
    private final int maxSteps;

    public SlidingMovement(List<Direction> directions, int maxSteps) {
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("maxSteps must be positive: " + maxSteps);
        }
        this.directions = List.copyOf(directions);
        this.maxSteps = maxSteps;
    }

    @Override
    public boolean canMove(Piece piece, Position from, Position to, Board board) {
        return getReachablePositions(piece, from, board).contains(to);
    }

    @Override
    public List<Position> getReachablePositions(Piece piece, Position from, Board board) {
        List<Position> result = new ArrayList<>();
        for (Direction direction : directions) {
            Position current = from.offset(direction);
            for (int step = 1; step <= maxSteps && board.isInside(current); step++) {
                Piece occupant = board.getPiece(current);
                if (occupant == null) {
                    result.add(current);
                } else {
                    if (occupant.getColor() != piece.getColor()) {
                        result.add(current);
                    }
                    break;
                }
                current = current.offset(direction);
            }
        }
        return result;
    }
}
