package ar.edu.uade.chess.core.board;

import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private final Board board = new Board(8, 8);

    @Test
    void constructor_rejectsNonPositiveDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new Board(0, 8));
        assertThrows(IllegalArgumentException.class, () -> new Board(8, -1));
    }

    @Test
    void anySize_isSupported() {
        Board big = new Board(64, 64);
        assertTrue(big.isInside(new Position(63, 63)));
        assertFalse(big.isInside(new Position(64, 0)));
    }

    @Test
    void placePiece_outsideBoard_throws() {
        Piece rook = new RookDefinition().create(Color.WHITE);
        assertThrows(IllegalArgumentException.class, () -> board.placePiece(rook, new Position(8, 8)));
    }

    @Test
    void movePiece_movesAndOverwritesDestination() {
        Piece rook = new RookDefinition().create(Color.WHITE);
        Piece enemy = new PawnDefinition().create(Color.BLACK);
        board.placePiece(rook, new Position(0, 0));
        board.placePiece(enemy, new Position(0, 5));

        board.movePiece(new Position(0, 0), new Position(0, 5));

        assertTrue(board.isEmpty(new Position(0, 0)));
        assertSame(rook, board.getPiece(new Position(0, 5)));
    }

    @Test
    void isPathClear_detectsBlockersOnLinesAndDiagonals() {
        board.placePiece(new PawnDefinition().create(Color.WHITE), new Position(0, 3));

        assertFalse(board.isPathClear(new Position(0, 0), new Position(0, 7)));
        assertTrue(board.isPathClear(new Position(0, 0), new Position(0, 3)), "destination itself is not checked");
        assertTrue(board.isPathClear(new Position(0, 0), new Position(7, 7)));
        assertFalse(board.isPathClear(new Position(0, 0), new Position(2, 1)), "not a straight line");
    }

    @Test
    void copy_isIndependentFromOriginal() {
        Piece rook = new RookDefinition().create(Color.WHITE);
        board.placePiece(rook, new Position(0, 0));

        Board copy = board.copy();
        copy.movePiece(new Position(0, 0), new Position(0, 1));
        copy.getPiece(new Position(0, 1)).setMoved(true);

        assertSame(rook, board.getPiece(new Position(0, 0)));
        assertFalse(rook.hasMoved());
    }

    @Test
    void move_promotionChoiceIsCarriedButIsNotPartOfTheIdentity() {
        Position from = new Position(6, 0);
        Position to = new Position(7, 0);
        Move promotion = new Move(from, to, "knight");

        assertEquals("knight", promotion.getPromotionId());
        assertEquals(new Move(from, to), promotion);
        assertEquals(new Move(from, to).hashCode(), promotion.hashCode());
    }
}
