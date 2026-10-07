package ar.edu.uade.chess.adapter;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.piece.SlidingMovement;
import ar.edu.uade.chess.core.port.PromotionOptions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Console promotion notation, tested in memory with a stub of the core's port. */
class PromotionNotationTest {

    private static Piece cacique(Color color) {
        return new Piece("cacique", color == Color.WHITE ? 'C' : 'c', color, 6, Set.of(),
                List.of(new SlidingMovement(Direction.ORTHOGONAL, 2)));
    }

    /** Stub: the choices the core would offer, already sorted by value. */
    private final PromotionOptions stub = color -> List.of(
            new QueenDefinition().create(color), cacique(color),
            new RookDefinition().create(color), new KnightDefinition().create(color));
    private final PromotionNotation notation = new PromotionNotation(stub);

    @Test
    void letterSpanishNameAndId_allSelectThePiece() {
        assertEquals("queen", notation.idFor("q", Color.WHITE));
        assertEquals("queen", notation.idFor("Reina", Color.BLACK));
        assertEquals("rook", notation.idFor("torre", Color.WHITE));
        assertEquals("knight", notation.idFor("n", Color.WHITE));
    }

    @Test
    void newPiece_isSelectableByItsOwnLetterAndId() {
        assertEquals("cacique", notation.idFor("c", Color.WHITE));
        assertEquals("cacique", notation.idFor("cacique", Color.BLACK));
    }

    @Test
    void unknownText_isPassedToTheCore() {
        assertEquals("x", notation.idFor("x", Color.WHITE));
    }

    @Test
    void help_listsEveryChoice() {
        assertEquals("q reina, c cacique, r torre, n caballo", notation.describe(Color.WHITE));
    }

    @Test
    void pieceNames_fallBackToTheIdForNewPieces() {
        assertEquals("Caballo", PieceNames.of(new KnightDefinition().create(Color.WHITE)));
        assertEquals("Cacique", PieceNames.of(cacique(Color.WHITE)));
    }
}
