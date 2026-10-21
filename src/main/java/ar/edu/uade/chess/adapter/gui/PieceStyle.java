package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.piece.Piece;

import java.awt.Color;
import java.awt.Font;

/**
 * How pieces are drawn (Strategy). The board, the sidebar and the previews draw every
 * piece through the style chosen for the game.
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
