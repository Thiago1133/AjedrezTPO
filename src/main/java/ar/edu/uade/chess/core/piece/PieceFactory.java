package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Color;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates pieces by id from registered definitions. The id is only a map key:
 * there is no switch or if over piece types.
 */
public class PieceFactory {
    private final Map<String, PieceDefinition> definitions = new LinkedHashMap<>();

    public PieceFactory(List<PieceDefinition> definitions) {
        definitions.forEach(this::register);
    }

    public void register(PieceDefinition definition) {
        definitions.put(definition.getId(), definition);
    }

    public Piece create(String id, Color color) {
        PieceDefinition definition = definitions.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown piece id: " + id);
        }
        return definition.create(color);
    }

    /** One new piece of each registered kind, in registration order. */
    public List<Piece> createAll(Color color) {
        List<Piece> pieces = new ArrayList<>();
        definitions.values().forEach(definition -> pieces.add(definition.create(color)));
        return pieces;
    }
}
