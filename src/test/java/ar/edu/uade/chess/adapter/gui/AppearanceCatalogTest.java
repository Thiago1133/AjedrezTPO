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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Piece styles and board themes, tested in memory: no window or Swing component is created. */
class AppearanceCatalogTest {
    private static final List<PieceDefinition> STANDARD = List.of(
            new KingDefinition(), new QueenDefinition(), new RookDefinition(),
            new BishopDefinition(), new KnightDefinition(), new PawnDefinition());

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
