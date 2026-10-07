package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.KingDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceDefinition;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Captured-pieces bookkeeping of the window, tested in memory without opening any window. */
class CapturedPiecesTrackerTest {

    private final Board board = new Board(8, 8);

    private static Position at(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    private void place(PieceDefinition definition, Color color, String square) {
        board.placePiece(definition.create(color), at(square));
    }

    /** Applies a simple move on a copy of the board, as the game would. */
    private Board afterMoving(Board before, String from, String to) {
        Board after = before.copy();
        after.removePiece(at(to));
        after.movePiece(at(from), at(to));
        return after;
    }

    private static List<String> ids(List<Piece> pieces) {
        return pieces.stream().map(Piece::getId).toList();
    }

    @Test
    void quietMove_capturesNothing() {
        place(new KnightDefinition(), Color.WHITE, "b1");
        CapturedPiecesTracker tracker = new CapturedPiecesTracker(board);

        tracker.recordMove(afterMoving(board, "b1", "c3"), Color.WHITE);

        assertTrue(tracker.capturedBy(Color.WHITE).isEmpty());
        assertEquals(0, tracker.advantageOf(Color.WHITE));
    }

    @Test
    void capture_isCreditedToTheCapturerWithItsAdvantage() {
        place(new RookDefinition(), Color.WHITE, "a1");
        place(new KnightDefinition(), Color.BLACK, "a5");
        CapturedPiecesTracker tracker = new CapturedPiecesTracker(board);

        tracker.recordMove(afterMoving(board, "a1", "a5"), Color.WHITE);

        assertEquals(List.of("knight"), ids(tracker.capturedBy(Color.WHITE)));
        assertTrue(tracker.capturedBy(Color.BLACK).isEmpty());
        assertEquals(3, tracker.advantageOf(Color.WHITE));
        assertEquals(0, tracker.advantageOf(Color.BLACK));
    }

    @Test
    void capturedPieces_areShownMostValuableFirst() {
        place(new RookDefinition(), Color.WHITE, "a1");
        place(new PawnDefinition(), Color.BLACK, "a5");
        place(new QueenDefinition(), Color.BLACK, "a8");
        CapturedPiecesTracker tracker = new CapturedPiecesTracker(board);

        Board afterPawn = afterMoving(board, "a1", "a5");
        tracker.recordMove(afterPawn, Color.WHITE);
        tracker.recordMove(afterMoving(afterPawn, "a5", "a8"), Color.WHITE);

        assertEquals(List.of("queen", "pawn"), ids(tracker.capturedBy(Color.WHITE)));
        assertEquals(10, tracker.advantageOf(Color.WHITE));
    }

    @Test
    void undo_restoresThePreviousState() {
        place(new RookDefinition(), Color.WHITE, "a1");
        place(new KnightDefinition(), Color.BLACK, "a5");
        CapturedPiecesTracker tracker = new CapturedPiecesTracker(board);
        tracker.recordMove(afterMoving(board, "a1", "a5"), Color.WHITE);

        assertTrue(tracker.undoLastMove());

        assertTrue(tracker.capturedBy(Color.WHITE).isEmpty());
        assertFalse(tracker.undoLastMove(), "nothing left to undo");
    }

    /** Redo in the window replays the move through recordMove, so it must give the same result. */
    @Test
    void undoThenRecordingTheSameMoveAgain_restoresTheSameCaptures() {
        place(new RookDefinition(), Color.WHITE, "a1");
        place(new KnightDefinition(), Color.BLACK, "a5");
        CapturedPiecesTracker tracker = new CapturedPiecesTracker(board);
        Board afterCapture = afterMoving(board, "a1", "a5");
        tracker.recordMove(afterCapture, Color.WHITE);
        tracker.undoLastMove();

        tracker.recordMove(afterCapture, Color.WHITE);

        assertEquals(List.of("knight"), ids(tracker.capturedBy(Color.WHITE)));
        assertEquals(3, tracker.advantageOf(Color.WHITE));
    }

    @Test
    void undoThenNewCapture_isComparedAgainstTheRestoredBoard() {
        place(new RookDefinition(), Color.WHITE, "a1");
        place(new KnightDefinition(), Color.BLACK, "a5");
        place(new PawnDefinition(), Color.BLACK, "h1");
        CapturedPiecesTracker tracker = new CapturedPiecesTracker(board);
        tracker.recordMove(afterMoving(board, "a1", "a5"), Color.WHITE);
        tracker.undoLastMove();

        tracker.recordMove(afterMoving(board, "a1", "h1"), Color.WHITE);

        assertEquals(List.of("pawn"), ids(tracker.capturedBy(Color.WHITE)));
    }

    @Test
    void promotion_doesNotCountTheMoversOwnPawn() {
        place(new PawnDefinition(), Color.WHITE, "a7");
        place(new KingDefinition(), Color.BLACK, "h8");
        CapturedPiecesTracker tracker = new CapturedPiecesTracker(board);
        Board after = board.copy();
        after.removePiece(at("a7"));
        after.placePiece(new QueenDefinition().create(Color.WHITE), at("a8"));

        tracker.recordMove(after, Color.WHITE);

        assertTrue(tracker.capturedBy(Color.WHITE).isEmpty());
        assertTrue(tracker.capturedBy(Color.BLACK).isEmpty());
    }

    @Test
    void enPassant_countsThePawnRemovedFromAnotherSquare() {
        place(new PawnDefinition(), Color.WHITE, "e5");
        place(new PawnDefinition(), Color.BLACK, "d5");
        CapturedPiecesTracker tracker = new CapturedPiecesTracker(board);
        Board after = board.copy();
        after.removePiece(at("d5"));
        after.movePiece(at("e5"), at("d6"));

        tracker.recordMove(after, Color.WHITE);

        assertEquals(List.of("pawn"), ids(tracker.capturedBy(Color.WHITE)));
    }

    @Test
    void reset_forgetsEverything() {
        place(new RookDefinition(), Color.WHITE, "a1");
        place(new KnightDefinition(), Color.BLACK, "a5");
        CapturedPiecesTracker tracker = new CapturedPiecesTracker(board);
        tracker.recordMove(afterMoving(board, "a1", "a5"), Color.WHITE);

        tracker.reset(board);

        assertTrue(tracker.capturedBy(Color.WHITE).isEmpty());
        assertFalse(tracker.undoLastMove());
    }
}
