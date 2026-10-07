package ar.edu.uade.chess.adapter.gui;

import java.awt.Color;
import java.util.List;

/**
 * The piece styles and board themes offered before each game. Adding a look means adding it
 * to these lists; the dialog, the board and the sidebar work with whatever is listed here.
 */
final class AppearanceCatalog {
    private static final List<PieceStyle> PIECE_STYLES = List.of(
            new ClassicPieceStyle(), new SolidPieceStyle(), new LetterPieceStyle());

    private static final List<BoardTheme> BOARD_THEMES = List.of(
            new BoardTheme("Verde",
                    new Color(238, 238, 210), new Color(118, 150, 86),
                    new Color(246, 246, 105), new Color(190, 210, 90),
                    new Color(78, 103, 57), new Color(231, 235, 211), new Color(65, 73, 58)),
            new BoardTheme("Madera",
                    new Color(240, 217, 181), new Color(181, 136, 99),
                    new Color(247, 236, 116), new Color(205, 210, 106),
                    new Color(150, 110, 78), new Color(245, 228, 200), new Color(92, 64, 44)),
            new BoardTheme("Azul",
                    new Color(222, 227, 230), new Color(140, 162, 173),
                    new Color(155, 199, 0), new Color(171, 201, 120),
                    new Color(100, 122, 135), new Color(232, 237, 240), new Color(55, 72, 84)),
            new BoardTheme("Gris",
                    new Color(220, 220, 220), new Color(140, 140, 140),
                    new Color(240, 214, 98), new Color(196, 190, 120),
                    new Color(110, 110, 110), new Color(235, 235, 235), new Color(60, 60, 60)));

    private AppearanceCatalog() {
    }

    static List<PieceStyle> pieceStyles() {
        return PIECE_STYLES;
    }

    static List<BoardTheme> boardThemes() {
        return BOARD_THEMES;
    }
}
