package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Color;

/**
 * Recipe for one kind of piece. Adding a new piece means adding a new
 * definition and registering it in the PieceFactory; nothing else changes.
 */
public interface PieceDefinition {
    String getId();

    Piece create(Color color);
}
