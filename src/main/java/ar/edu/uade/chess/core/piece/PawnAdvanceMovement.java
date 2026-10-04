package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.board.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Advances forward onto empty squares only (never captures).
 * One step normally, up to initialSteps on the piece's first move.
 */
public class PawnAdvanceMovement implements MovementRule {
    private final int initialSteps;

    public PawnAdvanceMovement(int initialSteps) {
        if (initialSteps <= 0) {
            throw new IllegalArgumentException("initialSteps must be positive: " + initialSteps);
        }
        this.initialSteps = initialSteps;
    }

    @Override
    public boolean canMove(Piece piece, Position from, Position to, Board board) {
        return getReachablePositions(piece, from, board).contains(to);
    }

    @Override
    public List<Position> getReachablePositions(Piece piece, Position from, Board board) {
        List<Position> result = new ArrayList<>();
        Direction forward = new Direction(piece.getColor().forward(), 0);
        int steps = piece.hasMoved() ? 1 : initialSteps;
        Position current = from.offset(forward);
        for (int step = 1; step <= steps && board.isInside(current) && board.isEmpty(current); step++) {
            result.add(current);
            current = current.offset(forward);
        }
        return result;
    }
}
