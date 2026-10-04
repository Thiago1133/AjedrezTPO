package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.board.Position;

import java.util.ArrayList;
import java.util.List;

/** Jumps straight to each offset, ignoring pieces in between (e.g. the knight). */
public class JumpMovement implements MovementRule {
    private final List<Direction> offsets;

    public JumpMovement(List<Direction> offsets) {
        this.offsets = List.copyOf(offsets);
    }

    @Override
    public boolean canMove(Piece piece, Position from, Position to, Board board) {
        return getReachablePositions(piece, from, board).contains(to);
    }

    @Override
    public List<Position> getReachablePositions(Piece piece, Position from, Board board) {
        List<Position> result = new ArrayList<>();
        for (Direction offset : offsets) {
            Position target = from.offset(offset);
            if (!board.isInside(target)) {
                continue;
            }
            Piece occupant = board.getPiece(target);
            if (occupant == null || occupant.getColor() != piece.getColor()) {
                result.add(target);
            }
        }
        return result;
    }
}
