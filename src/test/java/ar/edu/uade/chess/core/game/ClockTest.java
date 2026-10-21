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
 * Losing on time is a rule of the game, so it lives in the core: the window only reports
 * how many seconds passed (advanceClock) and the core decides who runs out.
 */
class ClockTest {
    private final PieceFactory pieceFactory = FullGameWiring.standardPieces();
    private final SpyGameObserver spy = new SpyGameObserver();

    private static Move move(String from, String to) {
        return new Move(new Position(from.charAt(1) - '1', from.charAt(0) - 'a'),
                new Position(to.charAt(1) - '1', to.charAt(0) - 'a'));
    }

    private Game start(TimeControl timeControl) {
        Board board = new Board(8, 8);
        new StandardChessSetup(pieceFactory).setup(board);
        Game game = FullGameWiring.twoPlayers(board, pieceFactory, timeControl);
        game.addObserver(spy);
        game.start();
        return game;
    }

    @Test
    void theClockStartsAfterWhitesFirstMove_andOnlyThePlayerToMoveLosesTime() {
        Game game = start(TimeControl.TEN_MINUTES);
        game.advanceClock(30);
        assertEquals(600, game.getRemainingTime(Color.WHITE), "white thinks its first move for free");

        assertTrue(game.move(move("e2", "e4")));
        game.advanceClock(30);

        assertEquals(570, game.getRemainingTime(Color.BLACK));
        assertEquals(600, game.getRemainingTime(Color.WHITE));
    }

    @Test
    void runningOutOfTime_losesTheGame_andNoMoreMovesArePlayed() {
        Game game = start(TimeControl.FIVE_MINUTES);
        assertTrue(game.move(move("e2", "e4")));

        game.advanceClock(300);

        assertEquals(GameStatus.BLACK_TIMEOUT, game.getStatus());
        assertTrue(spy.events.contains("over BLACK_TIMEOUT"));
        assertFalse(game.move(move("e7", "e5")));
        assertEquals(0, game.getRemainingTime(Color.BLACK));
    }

    /** As with a real chess clock, a loss on time is final: undo and redo do nothing. */
    @Test
    void aLossOnTime_cannotBeUndone() {
        Game game = start(TimeControl.FIVE_MINUTES);
        assertTrue(game.move(move("e2", "e4")));
        game.advanceClock(300);

        assertFalse(game.canUndo());
        game.undo();

        assertEquals(GameStatus.BLACK_TIMEOUT, game.getStatus());
        assertEquals("pawn", game.getBoard().getPiece(new Position(3, 4)).getId(), "e4 was not taken back");
        assertFalse(game.move(move("d2", "d4")));
    }

    @Test
    void withoutATimeLimit_theClockNeverRuns() {
        Game game = start(TimeControl.UNLIMITED);
        assertTrue(game.move(move("e2", "e4")));

        game.advanceClock(10_000);

        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertTrue(game.getRemainingTime(Color.BLACK) < 0, "unlimited is reported as a negative value");
    }
}
