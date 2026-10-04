package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;

import java.util.List;
import java.util.Set;

/** The queen is composed of the rook's and the bishop's movements. */
public class QueenDefinition implements PieceDefinition {

    @Override
    public String getId() {
        return "queen";
    }

    @Override
    public Piece create(Color color) {
        return new Piece(getId(), color == Color.WHITE ? 'Q' : 'q', color, 9,
                Set.of(),
                List.of(new SlidingMovement(Direction.ORTHOGONAL, Integer.MAX_VALUE),
                        new SlidingMovement(Direction.DIAGONAL, Integer.MAX_VALUE)));
    }
}
