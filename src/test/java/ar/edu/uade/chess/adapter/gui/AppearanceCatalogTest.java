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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Piece styles and board themes, tested in memory: no window or Swing component is created. */
class AppearanceCatalogTest {
    private static final List<PieceDefinition> STANDARD = List.of(
            new KingDefinition(), new QueenDefinition(), new RookDefinition(),
            new BishopDefinition(), new KnightDefinition(), new PawnDefinition());

    private static Piece cacique(Color color) {
        return new Piece("cacique", color == Color.WHITE ? 'C' : 'c', color, 7, Set.of(),
                List.of(new SlidingMovement(Direction.DIAGONAL, 2)));
    }

    @Test
    void catalog_offersThreeStylesAndFourThemes_withDistinctNames() {
        assertEquals(List.of("Clásicas", "Rellenas", "Letras"),
                AppearanceCatalog.pieceStyles().stream().map(PieceStyle::displayName).toList());
        assertEquals(List.of("Verde", "Madera", "Azul", "Gris"),
                AppearanceCatalog.boardThemes().stream().map(BoardTheme::displayName).toList());
    }

    @Test
    void everyStyle_drawsEveryStandardPiece_withDistinctSymbolsPerType() {
        for (PieceStyle style : AppearanceCatalog.pieceStyles()) {
            for (Color color : Color.values()) {
                Set<String> symbols = new HashSet<>();
                for (PieceDefinition definition : STANDARD) {
                    Piece piece = definition.create(color);
                    String symbol = style.symbolFor(piece);
                    assertFalse(symbol.isBlank(), style.displayName() + " " + definition.getId());
                    assertNotNull(style.fillFor(piece));
                    assertNotNull(style.font(40));
                    symbols.add(symbol);
                }
                assertEquals(STANDARD.size(), symbols.size(), style.displayName() + " repeats a symbol");
            }
        }
    }

    @Test
    void classicStyle_keepsTheUnicodeGlyphs_differentForEachColor() {
        PieceStyle classic = new ClassicPieceStyle();
        Map<PieceDefinition, List<String>> expected = Map.of(
                new KingDefinition(), List.of("♔", "♚"),
                new QueenDefinition(), List.of("♕", "♛"),
                new RookDefinition(), List.of("♖", "♜"),
                new BishopDefinition(), List.of("♗", "♝"),
                new KnightDefinition(), List.of("♘", "♞"),
                new PawnDefinition(), List.of("♙", "♟"));

        expected.forEach((definition, glyphs) -> {
            assertEquals(glyphs.get(0), classic.symbolFor(definition.create(Color.WHITE)));
            assertEquals(glyphs.get(1), classic.symbolFor(definition.create(Color.BLACK)));
        });
    }

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

    @Test
    void unknownPiece_isShownWithItsOwnLetterInEveryStyle() {
        for (PieceStyle style : AppearanceCatalog.pieceStyles()) {
            assertEquals("C", style.symbolFor(cacique(Color.WHITE)).toUpperCase(), style.displayName());
            assertEquals("C", style.symbolFor(cacique(Color.BLACK)).toUpperCase(), style.displayName());
        }
    }

    @Test
    void everyTheme_hasContrastingSquaresAndHighlights() {
        for (BoardTheme theme : AppearanceCatalog.boardThemes()) {
            assertNotEquals(theme.light(), theme.dark(), theme.displayName());
            assertNotEquals(theme.selected(), theme.light(), theme.displayName());
            assertNotEquals(theme.selected(), theme.dark(), theme.displayName());
            assertNotEquals(theme.lastMove(), theme.dark(), theme.displayName());
            assertNotNull(theme.marker(), theme.displayName());
            assertEquals(theme.light(), theme.squareColor(true));
            assertEquals(theme.dark(), theme.squareColor(false));
            assertNotEquals(theme.coordinateColor(true), theme.light(), theme.displayName());
            assertNotEquals(theme.coordinateColor(false), theme.dark(), theme.displayName());
        }
    }
}
