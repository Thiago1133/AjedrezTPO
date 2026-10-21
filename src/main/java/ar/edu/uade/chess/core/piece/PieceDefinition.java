package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Color;

/**
 * Recipe for one kind of piece: its id, symbol, value, traits and movement rules.
 */
public interface PieceDefinition {
    String getId();

    Piece create(Color color);
}
