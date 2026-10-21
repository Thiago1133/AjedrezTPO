package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.board.Position;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PieceFactoryTest {

    private final PieceFactory factory = new PieceFactory(List.of(
            new PawnDefinition(), new RookDefinition(), new KnightDefinition(),
            new BishopDefinition(), new QueenDefinition(), new KingDefinition()));

    @Test
    void create_buildsPieceOfRequestedIdAndColor() {
        Piece queen = factory.create("queen", Color.BLACK);

        assertEquals("queen", queen.getId());
        assertEquals(Color.BLACK, queen.getColor());
        assertEquals('q', queen.getSymbol());
        assertFalse(queen.hasMoved());
    }

    @Test
    void create_unknownId_throws() {
        assertThrows(IllegalArgumentException.class, () -> factory.create("dragon", Color.WHITE));
    }

    @Test
    void standardSetup_placesThirtyTwoPiecesInClassicOrder() {
        Board board = new Board(8, 8);
        new StandardChessSetup(factory).setup(board);

        assertEquals(16, board.getPositionsOf(Color.WHITE).size());
        assertEquals(16, board.getPositionsOf(Color.BLACK).size());
        assertEquals("king", board.getPiece(new Position(0, 4)).getId());
        assertEquals("queen", board.getPiece(new Position(7, 3)).getId());
        assertEquals("pawn", board.getPiece(new Position(6, 0)).getId());
        assertTrue(board.isEmpty(new Position(4, 4)));
    }
}
