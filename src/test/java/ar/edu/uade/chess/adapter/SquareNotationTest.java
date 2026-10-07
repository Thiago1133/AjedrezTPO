package ar.edu.uade.chess.adapter;

import ar.edu.uade.chess.core.board.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Square names for any board size, tested in memory (no console, no window). */
class SquareNotationTest {

    @Test
    void standardSquares_keepTheUsualNames() {
        assertEquals("a1", SquareNotation.square(new Position(0, 0)));
        assertEquals("e2", SquareNotation.square(new Position(1, 4)));
        assertEquals("h8", SquareNotation.square(new Position(7, 7)));
    }

    @Test
    void afterZ_columnsContinueWithTwoLetters() {
        assertEquals("z", SquareNotation.file(25));
        assertEquals("aa", SquareNotation.file(26));
        assertEquals("az", SquareNotation.file(51));
        assertEquals("ba", SquareNotation.file(52));
        assertEquals("bl", SquareNotation.file(63), "last column of a 64x64 board");
    }

    @Test
    void parse_isTheInverseOfTheNames_onA64x64Board() {
        for (int row = 0; row < 64; row++) {
            for (int column = 0; column < 64; column++) {
                Position position = new Position(row, column);
                assertEquals(position, SquareNotation.parse(SquareNotation.square(position)));
            }
        }
    }

    @Test
    void parse_acceptsUpperCase() {
        assertEquals(new Position(9, 26), SquareNotation.parse("AA10"));
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
