package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;

import java.util.List;
import java.util.Set;

public class BishopDefinition implements PieceDefinition {

    @Override
    public String getId() {
        return "bishop";
    }

    @Override
    public Piece create(Color color) {
        return new Piece(getId(), color == Color.WHITE ? 'B' : 'b', color, 3,
                Set.of(PieceTrait.MINOR, PieceTrait.COLOR_BOUND),
                List.of(new SlidingMovement(Direction.DIAGONAL, Integer.MAX_VALUE)));
    }
}
