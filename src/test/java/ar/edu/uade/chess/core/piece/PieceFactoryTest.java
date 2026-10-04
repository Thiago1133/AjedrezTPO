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
    void king_isTheOnlyRoyalPiece() {
        assertTrue(factory.create("king", Color.WHITE).hasTrait(PieceTrait.ROYAL));
        assertFalse(factory.create("queen", Color.WHITE).hasTrait(PieceTrait.ROYAL));
    }

    @Test
    void piece_copy_isIndependent() {
        Piece rook = factory.create("rook", Color.WHITE);
        rook.setMoved(true);

        Piece copy = rook.copy();
        copy.setMoved(false);

        assertNotSame(rook, copy);
        assertTrue(rook.hasMoved());
        assertEquals(rook.getId(), copy.getId());
    }

    /**
     * Extensibility check (Open/Closed): a brand new piece is added by writing a
     * definition that combines existing rules. No existing class is modified.
     */
    @Test
    void newPiece_canBeAddedWithoutModifyingExistingCode() {
        PieceDefinition cacique = new PieceDefinition() {
            @Override
            public String getId() {
                return "cacique";
            }

            @Override
            public Piece create(Color color) {
                return new Piece(getId(), 'C', color, 7, Set.of(),
                        List.of(new SlidingMovement(Direction.DIAGONAL, 2),
                                new JumpMovement(List.of(new Direction(3, 0), new Direction(-3, 0)))));
            }
        };
        factory.register(cacique);
        Board board = new Board(8, 8);
        Piece piece = factory.create("cacique", Color.WHITE);
        board.placePiece(piece, new Position(3, 3));

        assertTrue(piece.canMove(new Position(3, 3), new Position(5, 5), board));
        assertTrue(piece.canMove(new Position(3, 3), new Position(6, 3), board));
        assertFalse(piece.canMove(new Position(3, 3), new Position(6, 6), board));
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

    @Test
    void standardSetup_onTallerBoard_usesLastRowsForBlack() {
        Board board = new Board(12, 8);
        new StandardChessSetup(factory).setup(board);

        assertEquals(Color.BLACK, board.getPiece(new Position(11, 4)).getColor());
        assertEquals("pawn", board.getPiece(new Position(10, 0)).getId());
    }

    @Test
    void standardSetup_rejectsIncompatibleBoard() {
        assertThrows(IllegalArgumentException.class,
                () -> new StandardChessSetup(factory).setup(new Board(8, 10)));
    }
}
