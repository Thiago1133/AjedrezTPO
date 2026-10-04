package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Movement of each standard piece on an otherwise empty or simple board. No UI involved. */
class MovementRuleTest {

    private final Board board = new Board(8, 8);

    private Piece place(PieceDefinition definition, Color color, int row, int column) {
        Piece piece = definition.create(color);
        board.placePiece(piece, new Position(row, column));
        return piece;
    }

    private static Position at(int row, int column) {
        return new Position(row, column);
    }

    @Test
    void rook_movesInStraightLines() {
        Piece rook = place(new RookDefinition(), Color.WHITE, 3, 3);

        assertEquals(14, rook.getReachablePositions(at(3, 3), board).size());
        assertTrue(rook.canMove(at(3, 3), at(3, 7), board));
        assertTrue(rook.canMove(at(3, 3), at(0, 3), board));
        assertFalse(rook.canMove(at(3, 3), at(4, 4), board));
    }

    @Test
    void rook_isBlockedByOwnPiece_andCapturesFirstEnemy() {
        Piece rook = place(new RookDefinition(), Color.WHITE, 0, 0);
        place(new PawnDefinition(), Color.WHITE, 0, 3);
        place(new PawnDefinition(), Color.BLACK, 4, 0);

        assertTrue(rook.canMove(at(0, 0), at(0, 2), board));
        assertFalse(rook.canMove(at(0, 0), at(0, 3), board), "cannot capture own piece");
        assertFalse(rook.canMove(at(0, 0), at(0, 4), board), "cannot jump");
        assertTrue(rook.canMove(at(0, 0), at(4, 0), board), "captures enemy");
        assertFalse(rook.canMove(at(0, 0), at(5, 0), board), "stops at captured piece");
    }

    @Test
    void bishop_movesDiagonally() {
        Piece bishop = place(new BishopDefinition(), Color.WHITE, 3, 3);

        assertEquals(13, bishop.getReachablePositions(at(3, 3), board).size());
        assertTrue(bishop.canMove(at(3, 3), at(0, 0), board));
        assertTrue(bishop.canMove(at(3, 3), at(7, 7), board));
        assertFalse(bishop.canMove(at(3, 3), at(3, 5), board));
    }

    @Test
    void queen_combinesRookAndBishop() {
        Piece queen = place(new QueenDefinition(), Color.WHITE, 3, 3);

        assertEquals(27, queen.getReachablePositions(at(3, 3), board).size());
        assertTrue(queen.canMove(at(3, 3), at(3, 0), board));
        assertTrue(queen.canMove(at(3, 3), at(6, 6), board));
        assertFalse(queen.canMove(at(3, 3), at(5, 4), board));
    }

    @Test
    void king_movesOneSquareInAnyDirection() {
        Piece king = place(new KingDefinition(), Color.WHITE, 3, 3);

        assertEquals(8, king.getReachablePositions(at(3, 3), board).size());
        assertTrue(king.canMove(at(3, 3), at(4, 4), board));
        assertFalse(king.canMove(at(3, 3), at(5, 3), board));
    }

    @Test
    void king_inCorner_hasThreeMoves() {
        Piece king = place(new KingDefinition(), Color.WHITE, 0, 0);
        assertEquals(3, king.getReachablePositions(at(0, 0), board).size());
    }

    @Test
    void knight_jumpsOverPieces() {
        Piece knight = place(new KnightDefinition(), Color.WHITE, 0, 1);
        place(new PawnDefinition(), Color.WHITE, 1, 1);
        place(new PawnDefinition(), Color.WHITE, 1, 2);
        place(new PawnDefinition(), Color.WHITE, 1, 0);

        List<Position> reachable = knight.getReachablePositions(at(0, 1), board);

        assertEquals(3, reachable.size());
        assertTrue(reachable.containsAll(List.of(at(2, 0), at(2, 2), at(1, 3))));
    }

    @Test
    void knight_inCenter_hasEightMoves() {
        Piece knight = place(new KnightDefinition(), Color.BLACK, 4, 4);
        assertEquals(8, knight.getReachablePositions(at(4, 4), board).size());
    }

    @Test
    void whitePawn_canAdvanceOneOrTwoOnFirstMove() {
        Piece pawn = place(new PawnDefinition(), Color.WHITE, 1, 4);

        assertTrue(pawn.canMove(at(1, 4), at(2, 4), board));
        assertTrue(pawn.canMove(at(1, 4), at(3, 4), board));
        assertFalse(pawn.canMove(at(1, 4), at(4, 4), board));
        assertFalse(pawn.canMove(at(1, 4), at(0, 4), board), "cannot move backwards");
    }

    @Test
    void pawn_afterMoving_advancesOnlyOne() {
        Piece pawn = place(new PawnDefinition(), Color.WHITE, 2, 4);
        pawn.setMoved(true);

        assertTrue(pawn.canMove(at(2, 4), at(3, 4), board));
        assertFalse(pawn.canMove(at(2, 4), at(4, 4), board));
    }

    @Test
    void blackPawn_advancesDownTheBoard() {
        Piece pawn = place(new PawnDefinition(), Color.BLACK, 6, 4);

        assertTrue(pawn.canMove(at(6, 4), at(5, 4), board));
        assertTrue(pawn.canMove(at(6, 4), at(4, 4), board));
        assertFalse(pawn.canMove(at(6, 4), at(7, 4), board));
    }

    @Test
    void pawn_isBlockedAndCannotCaptureForward() {
        Piece pawn = place(new PawnDefinition(), Color.WHITE, 1, 4);
        place(new PawnDefinition(), Color.BLACK, 2, 4);

        assertFalse(pawn.canMove(at(1, 4), at(2, 4), board));
        assertFalse(pawn.canMove(at(1, 4), at(3, 4), board), "cannot jump over blocker");
    }

    @Test
    void pawn_capturesOnlyDiagonallyForward() {
        Piece pawn = place(new PawnDefinition(), Color.WHITE, 3, 3);
        place(new PawnDefinition(), Color.BLACK, 4, 4);
        place(new PawnDefinition(), Color.WHITE, 4, 2);

        assertTrue(pawn.canMove(at(3, 3), at(4, 4), board), "captures enemy");
        assertFalse(pawn.canMove(at(3, 3), at(4, 2), board), "not own piece");
        assertFalse(pawn.canMove(at(3, 3), at(2, 4), board), "not backwards");
    }

    @Test
    void slidingMovement_rejectsNonPositiveSteps() {
        assertThrows(IllegalArgumentException.class,
                () -> new SlidingMovement(ar.edu.uade.chess.core.board.Direction.ALL, 0));
    }
}
