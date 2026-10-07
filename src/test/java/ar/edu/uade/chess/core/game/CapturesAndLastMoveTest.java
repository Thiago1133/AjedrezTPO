package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Captured pieces, material advantage and the last move come from the core's own history
 * (each move command remembers what it took), so every adapter shows the same thing.
 */
class CapturesAndLastMoveTest {
    private final PieceFactory pieceFactory = FullGameWiring.standardPieces();
    private final Board board = new Board(8, 8);

    private static Position at(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    private static Move move(String from, String to) {
        return new Move(at(from), at(to));
    }

    private static List<String> ids(List<Piece> pieces) {
        return pieces.stream().map(Piece::getId).toList();
    }

    private void place(String id, Color color, String square) {
        board.placePiece(pieceFactory.create(id, color), at(square));
    }

    /** Both kings, plus a white pawn so no position starts as a draw by insufficient material. */
    private Game start() {
        place("king", Color.WHITE, "e1");
        place("king", Color.BLACK, "h8");
        place("pawn", Color.WHITE, "h2");
        Game game = FullGameWiring.twoPlayers(board, pieceFactory);
        game.start();
        return game;
    }

    @Test
    void atTheStart_nothingIsCapturedAndThereIsNoLastMove() {
        place("rook", Color.WHITE, "a1");
        Game game = start();

        assertTrue(game.getCapturedPieces(Color.WHITE).isEmpty());
        assertEquals(0, game.getMaterialAdvantage(Color.WHITE));
        assertNull(game.getLastMove());
    }

    @Test
    void quietMove_capturesNothing_butIsTheLastMove() {
        place("knight", Color.WHITE, "b1");
        Game game = start();

        assertTrue(game.move(move("b1", "c3")));

        assertTrue(game.getCapturedPieces(Color.WHITE).isEmpty());
        assertEquals(move("b1", "c3"), game.getLastMove());
    }

    @Test
    void capture_isCreditedToTheCapturer_withItsAdvantage() {
        place("rook", Color.WHITE, "a1");
        place("knight", Color.BLACK, "a5");
        Game game = start();

        assertTrue(game.move(move("a1", "a5")));

        assertEquals(List.of("knight"), ids(game.getCapturedPieces(Color.WHITE)));
        assertTrue(game.getCapturedPieces(Color.BLACK).isEmpty());
        assertEquals(3, game.getMaterialAdvantage(Color.WHITE));
        assertEquals(0, game.getMaterialAdvantage(Color.BLACK));
    }

    @Test
    void capturedPieces_areListedMostValuableFirst_andBothSidesCount() {
        place("rook", Color.WHITE, "a1");
        place("pawn", Color.BLACK, "a5");
        place("queen", Color.BLACK, "a8");
        place("bishop", Color.BLACK, "c5");
        place("knight", Color.WHITE, "d4");
        Game game = start();

        assertTrue(game.move(move("a1", "a5")));
        assertTrue(game.move(move("c5", "d4")));
        assertTrue(game.move(move("a5", "a8")));

        assertEquals(List.of("queen", "pawn"), ids(game.getCapturedPieces(Color.WHITE)));
        assertEquals(List.of("knight"), ids(game.getCapturedPieces(Color.BLACK)));
        assertEquals(7, game.getMaterialAdvantage(Color.WHITE), "10 captured against 3");
    }

    @Test
    void undo_removesTheCapture_andRestoresThePreviousLastMove_redoBringsThemBack() {
        place("rook", Color.WHITE, "a1");
        place("knight", Color.BLACK, "a5");
        Game game = start();
        assertTrue(game.move(move("a1", "a2")));
        assertTrue(game.move(move("h8", "g8")));
        assertTrue(game.move(move("a2", "a5")));

        game.undo();

        assertTrue(game.getCapturedPieces(Color.WHITE).isEmpty());
        assertEquals(move("h8", "g8"), game.getLastMove());

        game.redo();

        assertEquals(List.of("knight"), ids(game.getCapturedPieces(Color.WHITE)));
        assertEquals(move("a2", "a5"), game.getLastMove());
    }

    @Test
    void promotion_doesNotCountTheMoversOwnPawn_butCountsWhatItTakes() {
        place("pawn", Color.WHITE, "a7");
        place("pawn", Color.WHITE, "c7");
        place("rook", Color.BLACK, "d8");
        Game game = start();

        assertTrue(game.move(move("a7", "a8")));
        assertTrue(game.getCapturedPieces(Color.WHITE).isEmpty());
        assertTrue(game.getCapturedPieces(Color.BLACK).isEmpty());

        assertTrue(game.move(move("h8", "h7")));
        assertTrue(game.move(move("c7", "d8")));
        assertEquals(List.of("rook"), ids(game.getCapturedPieces(Color.WHITE)));
    }

    @Test
    void enPassant_countsThePawnTakenFromAnotherSquare() {
        place("pawn", Color.WHITE, "e5");
        place("pawn", Color.BLACK, "d7");
        Game game = start();
        assertTrue(game.move(move("e1", "f1")));
        assertTrue(game.move(move("d7", "d5")));

        assertTrue(game.move(move("e5", "d6")));

        assertEquals(List.of("pawn"), ids(game.getCapturedPieces(Color.WHITE)));
    }

    @Test
    void capturedPieces_areCopies_soAdaptersCannotChangeTheGame() {
        place("rook", Color.WHITE, "a1");
        place("knight", Color.BLACK, "a5");
        Game game = start();
        assertTrue(game.move(move("a1", "a5")));

        game.getCapturedPieces(Color.WHITE).get(0).setMoved(true);
        game.undo();

        assertFalse(game.getBoard().getPiece(at("a5")).hasMoved(), "the restored knight was not touched");
    }
}
