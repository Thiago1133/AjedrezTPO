package ar.edu.uade.chess.adapter;

import ar.edu.uade.chess.core.piece.Piece;

import java.util.Map;

/**
 * Spanish names shown to the player.
 */
public final class PieceNames {
    private static final Map<String, String> NAMES = Map.of(
            "king", "Rey", "queen", "Reina", "rook", "Torre",
            "bishop", "Alfil", "knight", "Caballo", "pawn", "Peón");

    private PieceNames() {
    }

    public static String of(Piece piece) {
        String id = piece.getId();
        String fallback = id.isEmpty() ? id : Character.toUpperCase(id.charAt(0)) + id.substring(1);
        return NAMES.getOrDefault(id, fallback);
    }
}
