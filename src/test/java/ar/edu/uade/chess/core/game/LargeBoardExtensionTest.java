package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.BoardSetup;
import ar.edu.uade.chess.core.piece.PieceFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Rehearsal of the live-defense extension "a 64x64 board": only the board size and a new
 * BoardSetup change; pieces, rules, check, end conditions and undo work unchanged.
 */
class LargeBoardExtensionTest {
    private static final int SIZE = 64;
    private static final int LAST = SIZE - 1;

    /** New setup for the big board: places exactly the given pieces (id per square). */
    static final class ScenarioSetup implements BoardSetup {
        private final PieceFactory pieceFactory;
        private final Map<Position, PieceAt> pieces;

        ScenarioSetup(PieceFactory pieceFactory, Map<Position, PieceAt> pieces) {
            this.pieceFactory = pieceFactory;
            this.pieces = Map.copyOf(pieces);
        }

        @Override
        public void setup(Board board) {
            pieces.forEach((position, piece) ->
                    board.placePiece(pieceFactory.create(piece.id(), piece.color()), position));
        }
    }

    record PieceAt(String id, Color color) {
    }

    private final SpyGameObserver spy = new SpyGameObserver();
    private final PieceFactory pieceFactory = FullGameWiring.standardPieces();

    private static Position at(int row, int column) {
        return new Position(row, column);
    }

    private Game startGame(Map<Position, PieceAt> pieces) {
        Board board = new Board(SIZE, SIZE);
        new ScenarioSetup(pieceFactory, pieces).setup(board);
        Game game = FullGameWiring.twoPlayers(board, pieceFactory);
        game.addObserver(spy);
        game.start();
        return game;
    }

    @Test
    void rook_slidesAcrossTheWholeBoard_andGivesCheckFromFar() {
        Game game = startGame(Map.of(
                at(0, 0), new PieceAt("king", Color.WHITE),
                at(5, 0), new PieceAt("rook", Color.WHITE),
                at(LAST, LAST), new PieceAt("king", Color.BLACK)));

        assertTrue(game.move(new Move(at(5, 0), at(LAST, 0))));

        assertEquals("rook", game.getBoard().getPiece(at(LAST, 0)).getId());
        assertEquals(GameStatus.CHECK, game.getStatus());
        assertTrue(spy.events.contains("check BLACK"));
    }

    /** King and bishop against king: the material rule already works by traits on any board. */
    @Test
    void insufficientMaterial_isDetectedOnTheBigBoard() {
        Game game = startGame(Map.of(
                at(0, 0), new PieceAt("king", Color.WHITE),
                at(10, 10), new PieceAt("bishop", Color.WHITE),
                at(LAST, LAST), new PieceAt("king", Color.BLACK)));

        assertEquals(GameStatus.DRAW, game.getStatus());
    }

    @Test
    void pawn_promotesOnTheLastRowOfTheBigBoard() {
        Game game = startGame(Map.of(
                at(0, 0), new PieceAt("king", Color.WHITE),
                at(LAST - 1, 10), new PieceAt("pawn", Color.WHITE),
                at(LAST, LAST), new PieceAt("king", Color.BLACK)));

        assertTrue(game.move(new Move(at(LAST - 1, 10), at(LAST, 10))));

        assertEquals("queen", game.getBoard().getPiece(at(LAST, 10)).getId());
    }

    @Test
    void checkmate_isDetectedInTheCorner_andUndoReopensTheGame() {
        Game game = startGame(Map.of(
                at(0, 0), new PieceAt("king", Color.WHITE),
                at(LAST - 1, 0), new PieceAt("rook", Color.WHITE),
                at(1, 1), new PieceAt("rook", Color.WHITE),
                at(LAST, LAST), new PieceAt("king", Color.BLACK)));

        assertTrue(game.move(new Move(at(1, 1), at(LAST, 1))));

        assertEquals(GameStatus.CHECKMATE, game.getStatus());
        assertTrue(spy.events.contains("over CHECKMATE"));
        assertFalse(game.move(new Move(at(LAST, LAST), at(LAST - 1, LAST - 1))), "game is over");

        game.undo();

        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertEquals("rook", game.getBoard().getPiece(at(1, 1)).getId());
    }

    @Test
    void illegalMoves_areStillRejected() {
        Game game = startGame(Map.of(
                at(0, 0), new PieceAt("king", Color.WHITE),
                at(10, 10), new PieceAt("bishop", Color.WHITE),
                at(1, 30), new PieceAt("pawn", Color.WHITE),
                at(LAST, LAST), new PieceAt("king", Color.BLACK)));
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus(), "the pawn avoids a draw by insufficient material");

        assertFalse(game.move(new Move(at(10, 10), at(10, 40))), "a bishop cannot move sideways");
        assertFalse(game.move(new Move(at(0, 0), at(0, 2))), "a king moves one square");
        assertTrue(game.move(new Move(at(10, 10), at(60, 60))), "but slides any distance diagonally");
    }
}
