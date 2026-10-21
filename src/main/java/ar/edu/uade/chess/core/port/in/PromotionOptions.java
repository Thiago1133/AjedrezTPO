package ar.edu.uade.chess.core.port.in;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.piece.Piece;

import java.util.List;

/**
 * Driving port: which pieces a pawn may promote to, so adapters offer exactly what the
 * core accepts.
 */
public interface PromotionOptions {
    /** One sample piece per valid choice, most valuable first. */
    List<Piece> getPromotionChoices(Color color);
}
