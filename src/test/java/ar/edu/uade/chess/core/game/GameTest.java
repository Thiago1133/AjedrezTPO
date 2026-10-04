package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.MoveCommandFactory;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.piece.BishopDefinition;
import ar.edu.uade.chess.core.piece.KingDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.piece.StandardChessSetup;
import ar.edu.uade.chess.core.rules.CheckDetector;
import ar.edu.uade.chess.core.rules.MoveValidator;
import ar.edu.uade.chess.core.status.CheckmateCondition;
import ar.edu.uade.chess.core.status.GameEndCondition;
import ar.edu.uade.chess.core.status.GameStatusEvaluator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The whole match runs in memory: no console, no rendering. */
class GameTest {

    private final SpyGameObserver spy = new SpyGameObserver();

    private static Position at(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    private static Move move(String from, String to) {
        return new Move(at(from), at(to));
    }

    private static Board standardBoard() {
        Board board = new Board(8, 8);
        new StandardChessSetup(new PieceFactory(List.of(
                new PawnDefinition(), new RookDefinition(), new KnightDefinition(),
                new BishopDefinition(), new QueenDefinition(), new KingDefinition()))).setup(board);
        return board;
    }

    private Game newGame(Board board, List<GameEndCondition> endConditions, List<Move> scriptedMoves) {
        CheckDetector checkDetector = new CheckDetector();
        MoveCommandFactory commandFactory = new MoveCommandFactory(List.of());
        FakeMoveInput input = new FakeMoveInput(scriptedMoves);
        TurnManager turns = new TurnManager(
                new Player(Color.WHITE, new HumanPlayerStrategy(input)),
                new Player(Color.BLACK, new HumanPlayerStrategy(input)));
        Game game = new Game(board, turns, new MoveValidator(checkDetector, commandFactory),
                new GameStatusEvaluator(checkDetector, endConditions), commandFactory, new MoveHistory());
        game.addObserver(spy);
        game.start();
        return game;
    }

    private Game newGame(Board board) {
        return newGame(board, List.of(), List.of());
    }

    @Test
    void start_whiteMovesFirst() {
        Game game = newGame(standardBoard());

        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(List.of("turn WHITE"), spy.events);
    }

    @Test
    void legalMove_updatesBoardAlternatesTurnAndNotifies() {
        Game game = newGame(standardBoard());

        assertTrue(game.move(move("e2", "e4")));

        assertEquals("pawn", game.getBoard().getPiece(at("e4")).getId());
        assertTrue(game.getBoard().isEmpty(at("e2")));
        assertEquals(Color.BLACK, game.getCurrentTurn());
        assertEquals(List.of("turn WHITE", "move " + move("e2", "e4"), "turn BLACK"), spy.events);
    }

    @Test
    void illegalMove_isRejectedAndNothingChanges() {
        Game game = newGame(standardBoard());

        assertFalse(game.move(move("e2", "e5")));

        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertEquals("pawn", game.getBoard().getPiece(at("e2")).getId());
    }

    @Test
    void cannotMoveOpponentPieceOnYourTurn() {
        Game game = newGame(standardBoard());

        assertFalse(game.move(move("e7", "e5")));
    }

    @Test
    void turnsAlternate() {
        Game game = newGame(standardBoard());

        assertTrue(game.move(move("e2", "e4")));
        assertFalse(game.move(move("d2", "d4")), "white cannot move twice");
        assertTrue(game.move(move("e7", "e5")));
        assertEquals(Color.WHITE, game.getCurrentTurn());
    }

    @Test
    void capture_removesEnemyPiece() {
        Game game = newGame(standardBoard());
        game.move(move("e2", "e4"));
        game.move(move("d7", "d5"));

        assertTrue(game.move(move("e4", "d5")));

        assertEquals(15, game.getBoard().getPositionsOf(Color.BLACK).size());
        assertEquals(Color.WHITE, game.getBoard().getPiece(at("d5")).getColor());
    }

    @Test
    void check_isDetectedAndAnnounced() {
        Board board = new Board(8, 8);
        board.placePiece(new KingDefinition().create(Color.WHITE), at("e1"));
        board.placePiece(new RookDefinition().create(Color.WHITE), at("a2"));
        board.placePiece(new KingDefinition().create(Color.BLACK), at("h8"));
        Game game = newGame(board);

        game.move(move("a2", "a8"));

        assertEquals(GameStatus.CHECK, game.getStatus());
        assertTrue(spy.events.contains("check BLACK"));
    }

    @Test
    void whenInCheck_moveThatIgnoresItIsRejected() {
        Board board = new Board(8, 8);
        board.placePiece(new KingDefinition().create(Color.WHITE), at("e1"));
        board.placePiece(new RookDefinition().create(Color.WHITE), at("a1"));
        board.placePiece(new KingDefinition().create(Color.BLACK), at("h8"));
        board.placePiece(new PawnDefinition().create(Color.BLACK), at("b7"));
        Game game = newGame(board);
        game.move(move("a1", "a8"));
        assertEquals(GameStatus.CHECK, game.getStatus());

        assertFalse(game.move(move("b7", "b6")), "pawn move leaves the king in check");
        assertTrue(game.move(move("h8", "h7")), "king escapes");
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
    }

    @Test
    void undo_restoresBoardAndTurn_redoReappliesIt() {
        Game game = newGame(standardBoard());
        game.move(move("e2", "e4"));

        game.undo();
        assertEquals("pawn", game.getBoard().getPiece(at("e2")).getId());
        assertTrue(game.getBoard().isEmpty(at("e4")));
        assertEquals(Color.WHITE, game.getCurrentTurn());

        game.redo();
        assertEquals("pawn", game.getBoard().getPiece(at("e4")).getId());
        assertEquals(Color.BLACK, game.getCurrentTurn());
    }

    @Test
    void undo_restoresCapturedPiece() {
        Game game = newGame(standardBoard());
        game.move(move("e2", "e4"));
        game.move(move("d7", "d5"));
        game.move(move("e4", "d5"));

        game.undo();

        assertEquals(Color.BLACK, game.getBoard().getPiece(at("d5")).getColor());
        assertEquals(16, game.getBoard().getPositionsOf(Color.BLACK).size());
    }

    @Test
    void undoAndRedo_withEmptyHistory_doNothing() {
        Game game = newGame(standardBoard());

        game.undo();
        game.redo();

        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertEquals(32, game.getBoard().getPositionsOf(Color.WHITE).size()
                + game.getBoard().getPositionsOf(Color.BLACK).size());
    }

    @Test
    void playTurn_asksTheCurrentPlayerStrategy() {
        Game game = newGame(standardBoard(), List.of(), List.of(move("e2", "e4"), move("e7", "e5")));

        assertTrue(game.playTurn());
        assertTrue(game.playTurn());
        assertFalse(game.playTurn(), "input exhausted: player offers no move");

        assertEquals("pawn", game.getBoard().getPiece(at("e5")).getId());
        assertEquals(Color.WHITE, game.getCurrentTurn());
    }

    @Test
    void getBoard_returnsCopyThatCannotAlterTheGame() {
        Game game = newGame(standardBoard());

        game.getBoard().removePiece(at("e1"));

        assertNotNull(game.getBoard().getPiece(at("e1")));
    }

    @Test
    void removedObserver_isNoLongerNotified() {
        Game game = newGame(standardBoard());
        game.removeObserver(spy);
        spy.events.clear();

        game.move(move("e2", "e4"));

        assertTrue(spy.events.isEmpty());
    }

    @Test
    void injectedEndCondition_endsTheGameAndBlocksMoves() {
        GameEndCondition alwaysOver = new GameEndCondition() {
            @Override
            public boolean isMet(Board board, MoveHistory history, Color toMove) {
                return true;
            }

            @Override
            public GameStatus getResult() {
                return GameStatus.DRAW;
            }
        };
        Game game = newGame(standardBoard(), List.of(alwaysOver), List.of(move("e2", "e4")));

        assertEquals(GameStatus.DRAW, game.getStatus());
        assertTrue(spy.events.contains("over DRAW"));
        assertFalse(game.move(move("e2", "e4")));
        assertFalse(game.playTurn());
    }

    @Test
    void foolsMate_endsTheGameWithCheckmate() {
        CheckDetector checkDetector = new CheckDetector();
        MoveValidator validator = new MoveValidator(checkDetector, new MoveCommandFactory(List.of()));
        Game game = newGame(standardBoard(), List.of(new CheckmateCondition(validator, checkDetector)), List.of());

        game.move(move("f2", "f3"));
        game.move(move("e7", "e5"));
        game.move(move("g2", "g4"));
        game.move(move("d8", "h4"));

        assertEquals(GameStatus.CHECKMATE, game.getStatus());
        assertTrue(spy.events.contains("over CHECKMATE"));
        assertFalse(game.move(move("a2", "a3")), "no moves after the game is over");
    }

    @Test
    void undoAfterCheckmate_resumesTheGame() {
        CheckDetector checkDetector = new CheckDetector();
        MoveValidator validator = new MoveValidator(checkDetector, new MoveCommandFactory(List.of()));
        Game game = newGame(standardBoard(), List.of(new CheckmateCondition(validator, checkDetector)), List.of());
        game.move(move("f2", "f3"));
        game.move(move("e7", "e5"));
        game.move(move("g2", "g4"));
        game.move(move("d8", "h4"));

        game.undo();

        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(Color.BLACK, game.getCurrentTurn());
    }

    @Test
    void getLegalMoves_listsMovesOfThePlayerToMove() {
        Game game = newGame(standardBoard());

        assertEquals(20, game.getLegalMoves().size());
        game.move(move("e2", "e4"));
        assertTrue(game.getLegalMoves().stream()
                .allMatch(m -> game.getBoard().getPiece(m.getFrom()).getColor() == Color.BLACK));
    }

    @Test
    void getLegalMoves_isEmptyWhenTheGameIsOver() {
        CheckDetector checkDetector = new CheckDetector();
        MoveValidator validator = new MoveValidator(checkDetector, new MoveCommandFactory(List.of()));
        Game game = newGame(standardBoard(), List.of(new CheckmateCondition(validator, checkDetector)), List.of());
        game.move(move("f2", "f3"));
        game.move(move("e7", "e5"));
        game.move(move("g2", "g4"));
        game.move(move("d8", "h4"));

        assertTrue(game.getLegalMoves().isEmpty());
    }

    @Test
    void turnManager_requiresWhiteThenBlack() {
        PlayerStrategy noMoves = (game, color) -> null;
        assertThrows(IllegalArgumentException.class, () -> new TurnManager(
                new Player(Color.BLACK, noMoves), new Player(Color.WHITE, noMoves)));
    }
}
