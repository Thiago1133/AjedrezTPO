package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.piece.Piece;

import java.util.Map;

/**
 * Translates the core's piece symbol into a Unicode chess glyph. Pieces without a
 * glyph (new piece types) are shown with their own symbol, so no code changes here.
 */
final class PieceGlyphs {
    private static final Map<Character, String> GLYPHS = Map.ofEntries(
            Map.entry('K', "♔"), Map.entry('k', "♚"),
            Map.entry('Q', "♕"), Map.entry('q', "♛"),
            Map.entry('R', "♖"), Map.entry('r', "♜"),
            Map.entry('B', "♗"), Map.entry('b', "♝"),
            Map.entry('N', "♘"), Map.entry('n', "♞"),
            Map.entry('P', "♙"), Map.entry('p', "♟"));

    private PieceGlyphs() { }

    static String of(Piece piece) {
        if (piece == null) return "";
        return GLYPHS.getOrDefault(piece.getSymbol(), String.valueOf(piece.getSymbol()));
    }
}
