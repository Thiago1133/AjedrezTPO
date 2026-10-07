package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.piece.Piece;

import java.awt.Color;
import java.awt.Font;

/**
 * How pieces are drawn (Strategy). A new look is a new implementation registered in
 * AppearanceCatalog; the board, the sidebar and the previews never change. Every style
 * must show pieces it does not know (new piece types) with their own symbol.
 */
interface PieceStyle {
    String displayName();

    /** Text to draw for the piece: a chess glyph or a letter. */
    String symbolFor(Piece piece);

    Font font(int size);

    Color fillFor(Piece piece);

    /** Outline drawn around the symbol, or null for none. */
    Color outlineFor(Piece piece);

    /** Thickness of that outline for a symbol of the given size; each style decides its own. */
    float outlineWidth(int fontSize);
}
