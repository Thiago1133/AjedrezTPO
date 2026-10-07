package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.piece.Piece;

import java.awt.Font;
import java.util.Map;

/**
 * The same solid silhouette for both sides, drawn with a thick outline: dark around white
 * pieces (so they stand out on light squares) and the fill color around black ones.
 */
final class SolidPieceStyle implements PieceStyle {
    private static final java.awt.Color WHITE_FILL = new java.awt.Color(250, 250, 248);
    private static final java.awt.Color WHITE_OUTLINE = new java.awt.Color(40, 40, 40);
    private static final java.awt.Color BLACK_FILL = new java.awt.Color(32, 32, 32);
    private static final java.awt.Color BLACK_OUTLINE = new java.awt.Color(32, 32, 32);
    private static final Map<Character, String> SILHOUETTES = Map.of(
            'K', "♚", 'Q', "♛", 'R', "♜", 'B', "♝", 'N', "♞", 'P', "♟");

    @Override
    public String displayName() {
        return "Rellenas";
    }

    @Override
    public String symbolFor(Piece piece) {
        char key = Character.toUpperCase(piece.getSymbol());
        return SILHOUETTES.getOrDefault(key, String.valueOf(piece.getSymbol()));
    }

    @Override
    public Font font(int size) {
        return new Font(Font.SERIF, Font.PLAIN, size);
    }

    @Override
    public java.awt.Color fillFor(Piece piece) {
        return piece.getColor() == Color.WHITE ? WHITE_FILL : BLACK_FILL;
    }

    @Override
    public java.awt.Color outlineFor(Piece piece) {
        return piece.getColor() == Color.WHITE ? WHITE_OUTLINE : BLACK_OUTLINE;
    }

    @Override
    public float outlineWidth(int fontSize) {
        return fontSize / 12f;
    }
}
