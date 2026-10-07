package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.piece.Piece;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

/** Outline glyphs for white and solid glyphs for black, in dark ink (the original look). */
final class ClassicPieceStyle implements PieceStyle {
    private static final Color INK = new Color(35, 38, 35);
    private static final Map<Character, String> GLYPHS = Map.ofEntries(
            Map.entry('K', "♔"), Map.entry('k', "♚"),
            Map.entry('Q', "♕"), Map.entry('q', "♛"),
            Map.entry('R', "♖"), Map.entry('r', "♜"),
            Map.entry('B', "♗"), Map.entry('b', "♝"),
            Map.entry('N', "♘"), Map.entry('n', "♞"),
            Map.entry('P', "♙"), Map.entry('p', "♟"));

    @Override
    public String displayName() {
        return "Clásicas";
    }

    @Override
    public String symbolFor(Piece piece) {
        return GLYPHS.getOrDefault(piece.getSymbol(), String.valueOf(piece.getSymbol()));
    }

    @Override
    public Font font(int size) {
        return new Font(Font.SERIF, Font.PLAIN, size);
    }

    @Override
    public Color fillFor(Piece piece) {
        return INK;
    }

    @Override
    public Color outlineFor(Piece piece) {
        return null;
    }
}
