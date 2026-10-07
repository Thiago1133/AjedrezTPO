package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.piece.Piece;

import java.awt.Font;

/** The piece's own letter (K, Q, R, B, N, P): works with any font and any piece type. */
final class LetterPieceStyle implements PieceStyle {
    private static final java.awt.Color WHITE_FILL = new java.awt.Color(250, 250, 248);
    private static final java.awt.Color WHITE_OUTLINE = new java.awt.Color(40, 40, 40);
    private static final java.awt.Color BLACK_FILL = new java.awt.Color(32, 32, 32);

    @Override
    public String displayName() {
        return "Letras";
    }

    @Override
    public String symbolFor(Piece piece) {
        return String.valueOf(Character.toUpperCase(piece.getSymbol()));
    }

    @Override
    public Font font(int size) {
        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(size * 0.8f));
    }

    @Override
    public java.awt.Color fillFor(Piece piece) {
        return piece.getColor() == Color.WHITE ? WHITE_FILL : BLACK_FILL;
    }

    @Override
    public java.awt.Color outlineFor(Piece piece) {
        return piece.getColor() == Color.WHITE ? WHITE_OUTLINE : null;
    }

    @Override
    public float outlineWidth(int fontSize) {
        return fontSize / 12f;
    }
}
