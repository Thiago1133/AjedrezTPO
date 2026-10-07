package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.StandardChessSetup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The core answers "is this a capture?" and "does this promote?" with its real rules,
 * so adapters never re-implement them. Answers never change the game.
 */
class MoveQueriesTest {
    private final PieceFactory pieceFactory = FullGameWiring.standardPieces();
    private final Board board = new Board(8, 8);

    private static Position at(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    private static Move move(String from, String to) {
        return new Move(at(from), at(to));
    }

    private void place(String id, Color color, String square) {
        board.placePiece(pieceFactory.create(id, color), at(square));
    }

    private Game start() {
        Game game = FullGameWiring.twoPlayers(board, pieceFactory);
        game.start();
        return game;
    }

    @Test
    void quietMove_isNeitherCaptureNorPromotion() {
        new StandardChessSetup(pieceFactory).setup(board);
        Game game = start();

        assertFalse(game.isCapture(move("e2", "e4")));
        assertFalse(game.isPromotion(move("e2", "e4")));
    }

    @Test
    void takingAPiece_isACapture() {
        place("king", Color.WHITE, "e1");
        place("king", Color.BLACK, "e8");
        place("rook", Color.WHITE, "a1");
        place("knight", Color.BLACK, "a5");
        Game game = start();

        assertTrue(game.isCapture(move("a1", "a5")));
        assertFalse(game.isCapture(move("a1", "a4")));
    }

    /** The destination is empty, yet it is a capture: only the real rule can tell. */
    @Test
    void enPassant_isACapture() {
        place("king", Color.WHITE, "e1");
        place("king", Color.BLACK, "e8");
        place("pawn", Color.WHITE, "e5");
        place("pawn", Color.BLACK, "d7");
        Game game = start();
        assertTrue(game.move(move("e1", "f1")));
        assertTrue(game.move(move("d7", "d5")));

        assertTrue(game.isCapture(move("e5", "d6")));
        assertFalse(game.isCapture(move("e5", "e6")));
    }

    @Test
    void castling_isNotACapture() {
        place("king", Color.WHITE, "e1");
        place("rook", Color.WHITE, "h1");
        place("king", Color.BLACK, "e8");
        Game game = start();

        assertTrue(game.getLegalMoves().contains(move("e1", "g1")));
        assertFalse(game.isCapture(move("e1", "g1")));
    }

    @Test
    void reachingTheLastRow_isAPromotion_alsoWhenCapturing() {
        place("king", Color.WHITE, "e1");
        place("king", Color.BLACK, "h6");
        place("pawn", Color.WHITE, "a7");
        place("rook", Color.BLACK, "b8");
        Game game = start();

        assertTrue(game.isPromotion(move("a7", "a8")));
        assertFalse(game.isCapture(move("a7", "a8")));
        assertTrue(game.isPromotion(move("a7", "b8")));
        assertTrue(game.isCapture(move("a7", "b8")));
    }

    @Test
    void illegalMove_isNeitherCaptureNorPromotion() {
        place("king", Color.WHITE, "e1");
        place("king", Color.BLACK, "e8");
        place("pawn", Color.WHITE, "a7");
        place("knight", Color.BLACK, "c6");
        Game game = start();

        assertFalse(game.isPromotion(move("a7", "b8")), "a pawn cannot move diagonally onto an empty square");
        assertFalse(game.isCapture(move("e1", "c6")), "a king cannot jump to c6");
    }

    @Test
    void asking_doesNotChangeTheGame() {
        place("king", Color.WHITE, "e1");
        place("king", Color.BLACK, "e8");
        place("rook", Color.WHITE, "a1");
        place("knight", Color.BLACK, "a5");
        Game game = start();
        int legalMovesBefore = game.getLegalMoves().size();

        game.isCapture(move("a1", "a5"));
        game.isPromotion(move("a1", "a5"));

        assertEquals("rook", game.getBoard().getPiece(at("a1")).getId());
        assertEquals("knight", game.getBoard().getPiece(at("a5")).getId());
        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertEquals(legalMovesBefore, game.getLegalMoves().size());
        game.undo();
        assertEquals("rook", game.getBoard().getPiece(at("a1")).getId(), "nothing was added to the history");
    }
}
