package ar.edu.uade.chess.core.rules;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.BishopDefinition;
import ar.edu.uade.chess.core.piece.KingDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.PieceDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CheckDetectorTest {

    private final Board board = new Board(8, 8);
    private final CheckDetector detector = new CheckDetector();

    private void place(PieceDefinition definition, Color color, int row, int column) {
        board.placePiece(definition.create(color), new Position(row, column));
    }

    @Test
    void rookOnOpenFile_givesCheck() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new RookDefinition(), Color.BLACK, 7, 4);

        assertTrue(detector.isInCheck(board, Color.WHITE));
        assertFalse(detector.isInCheck(board, Color.BLACK));
    }

    @Test
    void blockedRook_doesNotGiveCheck() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new PawnDefinition(), Color.WHITE, 1, 4);
        place(new RookDefinition(), Color.BLACK, 7, 4);

        assertFalse(detector.isInCheck(board, Color.WHITE));
    }

    @Test
    void knight_givesCheckOverPieces() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new PawnDefinition(), Color.WHITE, 1, 4);
        place(new PawnDefinition(), Color.WHITE, 1, 3);
        place(new KnightDefinition(), Color.BLACK, 2, 3);

        assertTrue(detector.isInCheck(board, Color.WHITE));
    }

    @Test
    void pawn_givesCheckDiagonallyButNotForward() {
        place(new KingDefinition(), Color.WHITE, 3, 4);
        place(new PawnDefinition(), Color.BLACK, 4, 4);
        assertFalse(detector.isInCheck(board, Color.WHITE), "pawn in front does not attack");

        place(new PawnDefinition(), Color.BLACK, 4, 5);
        assertTrue(detector.isInCheck(board, Color.WHITE));
    }

    @Test
    void bishop_givesCheckOnDiagonal() {
        place(new KingDefinition(), Color.BLACK, 7, 7);
        place(new BishopDefinition(), Color.WHITE, 0, 0);

        assertTrue(detector.isInCheck(board, Color.BLACK));
    }

    @Test
    void withoutRoyalPieces_thereIsNoCheck() {
        place(new RookDefinition(), Color.BLACK, 7, 4);
        assertFalse(detector.isInCheck(board, Color.WHITE));
    }

    @Test
    void isSquareAttacked_countsPawnAttacksOnEmptySquares() {
        place(new PawnDefinition(), Color.BLACK, 6, 4);

        assertTrue(detector.isSquareAttacked(board, new Position(5, 3), Color.BLACK));
        assertTrue(detector.isSquareAttacked(board, new Position(5, 5), Color.BLACK));
        assertFalse(detector.isSquareAttacked(board, new Position(5, 4), Color.BLACK));
    }

    @Test
    void isSquareAttacked_doesNotModifyBoard() {
        place(new PawnDefinition(), Color.BLACK, 6, 4);

        detector.isSquareAttacked(board, new Position(5, 3), Color.BLACK);

        assertTrue(board.isEmpty(new Position(5, 3)));
    }

    @Test
    void findRoyalPieces_returnsEveryRoyalPiece() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new KingDefinition(), Color.WHITE, 0, 0);
        place(new RookDefinition(), Color.WHITE, 0, 7);

        List<Position> royals = detector.findRoyalPieces(board, Color.WHITE);

        assertEquals(2, royals.size());
        assertTrue(royals.containsAll(List.of(new Position(0, 4), new Position(0, 0))));
    }

    @Test
    void check_worksOnBigBoards() {
        Board big = new Board(64, 64);
        big.placePiece(new KingDefinition().create(Color.WHITE), new Position(0, 0));
        big.placePiece(new BishopDefinition().create(Color.BLACK), new Position(63, 63));

        assertTrue(detector.isInCheck(big, Color.WHITE));
    }
}
