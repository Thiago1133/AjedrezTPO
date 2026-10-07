package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.StandardChessSetup;
import ar.edu.uade.chess.core.rules.CheckDetector;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Undo and redo give the turn back to a human: against the computer they also take back
 * (or replay) its reply. This used to be written separately in each adapter.
 */
class UndoRedoByTurnTest {
    private final PieceFactory pieceFactory = FullGameWiring.standardPieces();
    private final SpyGameObserver spy = new SpyGameObserver();

    private static Position at(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    private static Move move(String from, String to) {
        return new Move(at(from), at(to));
    }

    private Game start(boolean versusComputer) {
        Board board = new Board(8, 8);
        new StandardChessSetup(pieceFactory).setup(board);
        Game game = versusComputer
                ? FullGameWiring.versusComputer(board, pieceFactory)
                : FullGameWiring.twoPlayers(board, pieceFactory);
        game.addObserver(spy);
        game.start();
        return game;
    }

    /** The human plays e2-e4 and the computer answers, as the window and the console do. */
    private Game afterOneRoundAgainstTheComputer() {
        Game game = start(true);
        assertTrue(game.move(move("e2", "e4")));
        assertTrue(game.playTurn(), "the computer answers");
        assertEquals(Color.WHITE, game.getCurrentTurn());
        return game;
    }

    @Test
    void atTheStart_thereIsNothingToUndoOrRedo() {
        Game game = start(true);

        assertFalse(game.canUndo());
        assertFalse(game.canRedo());
    }

    @Test
    void againstTheComputer_undoTakesBackItsReplyAndTheHumanMove() {
        Game game = afterOneRoundAgainstTheComputer();

        game.undo();

        assertEquals(Color.WHITE, game.getCurrentTurn(), "the human is to move again");
        assertEquals("pawn", game.getBoard().getPiece(at("e2")).getId());
        assertNull(game.getLastMove());
        assertFalse(game.canUndo());
        assertTrue(game.canRedo());
    }

    @Test
    void againstTheComputer_redoReplaysBothMoves() {
        Game game = afterOneRoundAgainstTheComputer();
        Move computerReply = game.getLastMove();
        game.undo();

        game.redo();

        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertEquals("pawn", game.getBoard().getPiece(at("e4")).getId());
        assertEquals(computerReply, game.getLastMove());
        assertFalse(game.canRedo());
    }

    @Test
    void againstTheComputer_undoNotifiesTheTurnOnlyOnce() {
        Game game = afterOneRoundAgainstTheComputer();
        spy.events.clear();

        game.undo();

        assertEquals(List.of("turn WHITE"), spy.events, "adapters refresh once, already on the human's turn");
    }

    @Test
    void twoPlayers_undoAndRedoOneMoveAtATime() {
        Game game = start(false);
        assertTrue(game.move(move("e2", "e4")));
        assertTrue(game.move(move("e7", "e5")));

        game.undo();
        assertEquals(Color.BLACK, game.getCurrentTurn());
        assertEquals(move("e2", "e4"), game.getLastMove());

        game.undo();
        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertFalse(game.canUndo());

        game.redo();
        assertEquals(Color.BLACK, game.getCurrentTurn());
        assertTrue(game.canRedo());
    }

    @Test
    void aNewMoveAfterUndo_discardsWhatCouldBeRedone() {
        Game game = afterOneRoundAgainstTheComputer();
        game.undo();

        assertTrue(game.move(move("d2", "d4")));

        assertFalse(game.canRedo());
        assertTrue(game.canUndo());
    }

    @Test
    void onlyTheComputerIsAutomatic() {
        assertTrue(new AIPlayerStrategy(new MaterialEvaluationStrategy(new CheckDetector()), new Random(0)).isAutomatic());
        assertFalse(new HumanPlayerStrategy(color -> null).isAutomatic());
    }
}
