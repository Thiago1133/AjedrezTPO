package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Piece styles and board themes, tested in memory: no window or Swing component is created. */
class AppearanceCatalogTest {
    /** Solid and letter styles use the same shape for both sides, so color must tell them apart. */
    @Test
    void solidAndLetterStyles_distinguishSidesByFill() {
        for (PieceStyle style : List.of(new SolidPieceStyle(), new LetterPieceStyle())) {
            Piece white = new QueenDefinition().create(Color.WHITE);
            Piece black = new QueenDefinition().create(Color.BLACK);
            assertEquals(style.symbolFor(white), style.symbolFor(black), style.displayName());
            assertNotEquals(style.fillFor(white), style.fillFor(black), style.displayName());
            assertNotNull(style.outlineFor(white), "white needs an outline on light squares");
            assertNotEquals(style.fillFor(white), style.outlineFor(white),
                    style.displayName() + ": a white outline would vanish on light squares");
            assertTrue(style.outlineWidth(40) > 0, style.displayName());
        }
    }
}
