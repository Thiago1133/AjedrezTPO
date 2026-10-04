package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Color;

import java.util.List;
import java.util.Set;

public class PawnDefinition implements PieceDefinition {

    @Override
    public String getId() {
        return "pawn";
    }

    @Override
    public Piece create(Color color) {
        return new Piece(getId(), color == Color.WHITE ? 'P' : 'p', color, 1,
                Set.of(PieceTrait.PROMOTES, PieceTrait.EN_PASSANT),
                List.of(new PawnAdvanceMovement(2), new PawnCaptureMovement()));
    }
}
