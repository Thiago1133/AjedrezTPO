package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;

import java.util.List;
import java.util.Set;

public class KnightDefinition implements PieceDefinition {
    private static final List<Direction> KNIGHT_JUMPS = List.of(
            new Direction(2, 1), new Direction(2, -1), new Direction(-2, 1), new Direction(-2, -1),
            new Direction(1, 2), new Direction(1, -2), new Direction(-1, 2), new Direction(-1, -2));

    @Override
    public String getId() {
        return "knight";
    }

    @Override
    public Piece create(Color color) {
        return new Piece(getId(), color == Color.WHITE ? 'N' : 'n', color, 3,
                Set.of(),
                List.of(new JumpMovement(KNIGHT_JUMPS)));
    }
}
