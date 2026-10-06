package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.piece.BishopDefinition;
import ar.edu.uade.chess.core.piece.KingDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceDefinition;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.piece.SlidingMovement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Pure in-memory tests: PieceGlyphs only depends on Piece, so no Swing component is created. */
class PieceGlyphsTest {

    @Test
    void standardPieces_haveTheirUnicodeGlyph_differentForEachColor() {
        Map<PieceDefinition, List<String>> expected = Map.of(
                new KingDefinition(), List.of("♔", "♚"),
                new QueenDefinition(), List.of("♕", "♛"),
                new RookDefinition(), List.of("♖", "♜"),
                new BishopDefinition(), List.of("♗", "♝"),
                new KnightDefinition(), List.of("♘", "♞"),
                new PawnDefinition(), List.of("♙", "♟"));

        expected.forEach((definition, glyphs) -> {
            String white = PieceGlyphs.of(definition.create(Color.WHITE));
            String black = PieceGlyphs.of(definition.create(Color.BLACK));
            assertEquals(glyphs.get(0), white, definition.getId() + " blanco");
            assertEquals(glyphs.get(1), black, definition.getId() + " negro");
            assertNotEquals(white, black, definition.getId());
        });
    }

    @Test
    void unknownPiece_isShownWithItsOwnSymbol() {
        Piece cacique = new Piece("cacique", 'C', Color.WHITE, 7, Set.of(),
                List.of(new SlidingMovement(Direction.DIAGONAL, 2)));

        assertEquals("C", PieceGlyphs.of(cacique));
    }

    @Test
    void nullPiece_isShownAsEmpty() {
        assertEquals("", PieceGlyphs.of(null));
    }
}
