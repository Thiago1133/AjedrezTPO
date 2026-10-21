package ar.edu.uade.chess.core.rules;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.piece.BishopDefinition;
import ar.edu.uade.chess.core.piece.KingDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceDefinition;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.piece.SlidingMovement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Which pieces a pawn may promote to. */
class PromotionChoicesTest {

    private final PieceFactory pieceFactory = new PieceFactory(List.of(
            new PawnDefinition(), new RookDefinition(), new KnightDefinition(),
            new BishopDefinition(), new QueenDefinition(), new KingDefinition()));
    private final PromotionRule rule = new PromotionRule(pieceFactory);

    private static List<String> ids(List<Piece> pieces) {
        return pieces.stream().map(Piece::getId).toList();
    }

    /** Knight and bishop are worth the same: ties keep the factory's registration order. */
    @Test
    void choices_excludeRoyalAndPromotingPieces_mostValuableFirst() {
        assertEquals(List.of("queen", "rook", "knight", "bishop"), ids(rule.getPromotionChoices(Color.WHITE)));
    }
}
