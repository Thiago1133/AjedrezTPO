package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;

import java.util.List;
import java.util.Set;

/** The king slides one square in any direction and is the royal piece. */
public class KingDefinition implements PieceDefinition {

    @Override
    public String getId() {
        return "king";
    }

    @Override
    public Piece create(Color color) {
        return new Piece(getId(), color == Color.WHITE ? 'K' : 'k', color, 1000,
                Set.of(PieceTrait.ROYAL, PieceTrait.CASTLES),
                List.of(new SlidingMovement(Direction.ALL, 1)));
    }
}
