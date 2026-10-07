package ar.edu.uade.chess.adapter;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.port.in.PromotionOptions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Console notation for promotions, derived from the choices the core accepts: each piece
 * can be written by its letter, its Spanish name or its id ("q", "reina", "queen").
 */
final class PromotionNotation {
    private final PromotionOptions options;

    PromotionNotation(PromotionOptions options) {
        this.options = options;
    }

    /** The piece id for what the player typed; unknown text is passed on and the core uses its default. */
    String idFor(String typed, Color color) {
        return aliases(color).getOrDefault(typed.toLowerCase(), typed);
    }

    /** For the help text, e.g. "q reina, r torre, b alfil, n caballo". */
    String describe(Color color) {
        StringJoiner text = new StringJoiner(", ");
        for (Piece piece : options.getPromotionChoices(color)) {
            text.add(letterOf(piece) + " " + PieceNames.of(piece).toLowerCase());
        }
        return text.toString();
    }

    private Map<String, String> aliases(Color color) {
        Map<String, String> aliases = new LinkedHashMap<>();
        // Most valuable first: if two pieces share a letter, the letter keeps the first one.
        for (Piece piece : options.getPromotionChoices(color)) {
            aliases.putIfAbsent(letterOf(piece), piece.getId());
            aliases.putIfAbsent(PieceNames.of(piece).toLowerCase(), piece.getId());
            aliases.putIfAbsent(piece.getId(), piece.getId());
        }
        return aliases;
    }

    private static String letterOf(Piece piece) {
        return String.valueOf(Character.toLowerCase(piece.getSymbol()));
    }
}
