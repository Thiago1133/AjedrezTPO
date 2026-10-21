package ar.edu.uade.chess.adapter;

import ar.edu.uade.chess.core.board.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Square names, tested in memory (no console, no window). */
class SquareNotationTest {

    @Test
    void standardSquares_keepTheUsualNames() {
        assertEquals("a1", SquareNotation.square(new Position(0, 0)));
        assertEquals("e2", SquareNotation.square(new Position(1, 4)));
        assertEquals("h8", SquareNotation.square(new Position(7, 7)));
    }

    @Test
    void parse_isTheInverseOfTheNames() {
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Position position = new Position(row, column);
                assertEquals(position, SquareNotation.parse(SquareNotation.square(position)));
            }
        }
    }

    @Test
    void parse_rejectsTextThatIsNotASquare() {
        assertNull(SquareNotation.parse("e"));
        assertNull(SquareNotation.parse("22"));
        assertNull(SquareNotation.parse("e2x"));
        assertNull(SquareNotation.parse("ñ2"));
        assertNull(SquareNotation.parse(""));
    }
}
