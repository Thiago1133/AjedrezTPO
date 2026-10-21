package ar.edu.uade.chess.adapter;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.piece.BishopDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.port.in.PromotionOptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Console promotion notation, tested in memory with a stub of the core's port. */
class PromotionNotationTest {

    /** Stub: the choices the core would offer, already sorted by value. */
    private final PromotionOptions stub = color -> List.of(
            new QueenDefinition().create(color), new RookDefinition().create(color),
            new KnightDefinition().create(color), new BishopDefinition().create(color));
    private final PromotionNotation notation = new PromotionNotation(stub);

    @Test
    void letterSpanishNameAndId_allSelectThePiece() {
        assertEquals("queen", notation.idFor("q", Color.WHITE));
        assertEquals("queen", notation.idFor("Reina", Color.BLACK));
        assertEquals("rook", notation.idFor("torre", Color.WHITE));
        assertEquals("knight", notation.idFor("n", Color.WHITE));
    }
}
