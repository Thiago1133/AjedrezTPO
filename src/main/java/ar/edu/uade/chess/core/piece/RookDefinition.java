package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;

import java.util.List;
import java.util.Set;

public class RookDefinition implements PieceDefinition {

    @Override
    public String getId() {
        return "rook";
    }

    @Override
    public Piece create(Color color) {
        return new Piece(getId(), color == Color.WHITE ? 'R' : 'r', color, 5,
                Set.of(PieceTrait.CASTLES),
                List.of(new SlidingMovement(Direction.ORTHOGONAL, Integer.MAX_VALUE)));
    }
}
