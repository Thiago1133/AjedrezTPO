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
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceDefinition;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.piece.StandardChessSetup;
import ar.edu.uade.chess.core.rules.CheckDetector;
import ar.edu.uade.chess.core.rules.MoveValidator;
import ar.edu.uade.chess.core.status.GameStatusEvaluator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AIPlayerStrategyTest {

    private final Board board = new Board(8, 8);
    private final CheckDetector checkDetector = new CheckDetector();
    private final MaterialEvaluationStrategy evaluator = new MaterialEvaluationStrategy(checkDetector);

    private static Position at(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    private static Move move(String from, String to) {
        return new Move(at(from), at(to));
    }

    private Piece place(PieceDefinition definition, Color color, String square) {
        Piece piece = definition.create(color);
        board.placePiece(piece, at(square));
        return piece;
    }

    private AIPlayerStrategy ai() {
        return new AIPlayerStrategy(evaluator, new Random(42));
    }

    @Test
    void evaluator_scoresCaptureByValueOfCapturedPiece() {
        place(new RookDefinition(), Color.BLACK, "a8");
        place(new QueenDefinition(), Color.WHITE, "a1");

        assertEquals(9, evaluator.evaluate(board, move("a8", "a1"), Color.BLACK));
        assertEquals(0, evaluator.evaluate(board, move("a8", "b8"), Color.BLACK));
    }

    @Test
    void evaluator_penalizesLandingOnAnAttackedSquare() {
        place(new QueenDefinition(), Color.BLACK, "d8");
        place(new PawnDefinition(), Color.WHITE, "e3");

        assertEquals(-9, evaluator.evaluate(board, move("d8", "d4"), Color.BLACK), "pawn on e3 takes the queen");
        assertEquals(0, evaluator.evaluate(board, move("d8", "d5"), Color.BLACK));
    }

    @Test
    void evaluator_capturingADefendedPieceWeighsBothSides() {
        place(new QueenDefinition(), Color.BLACK, "d8");
        place(new KnightDefinition(), Color.WHITE, "d4");
        place(new PawnDefinition(), Color.WHITE, "e3");

        assertEquals(3 - 9, evaluator.evaluate(board, move("d8", "d4"), Color.BLACK));
    }

    @Test
    void evaluator_rewardsPromotion() {
        place(new PawnDefinition(), Color.BLACK, "b2").setMoved(true);

        assertEquals(8, evaluator.evaluate(board, move("b2", "b1"), Color.BLACK));
    }

    @Test
    void evaluator_doesNotModifyTheBoard() {
        place(new RookDefinition(), Color.BLACK, "a8");
        place(new QueenDefinition(), Color.WHITE, "a1");

        evaluator.evaluate(board, move("a8", "a1"), Color.BLACK);

        assertEquals("rook", board.getPiece(at("a8")).getId());
        assertEquals("queen", board.getPiece(at("a1")).getId());
    }

    @Test
    void ai_choosesTheMostValuableSafeCapture() {
        place(new RookDefinition(), Color.BLACK, "d8");
        place(new QueenDefinition(), Color.WHITE, "d1");
        place(new PawnDefinition(), Color.WHITE, "h8");
        List<Move> legal = List.of(move("d8", "d1"), move("d8", "h8"), move("d8", "d5"));

        Move chosen = ai().chooseMove(new FakeGameQueries(board, legal), Color.BLACK);

        assertEquals(move("d8", "d1"), chosen);
    }

    @Test
    void ai_avoidsHangingItsPieces() {
        place(new QueenDefinition(), Color.BLACK, "d8");
        place(new PawnDefinition(), Color.WHITE, "e3");
        List<Move> legal = List.of(move("d8", "d4"), move("d8", "d6"));

        Move chosen = ai().chooseMove(new FakeGameQueries(board, legal), Color.BLACK);

        assertEquals(move("d8", "d6"), chosen);
    }

    @Test
    void ai_withNoLegalMoves_returnsNull() {
        assertNull(ai().chooseMove(new FakeGameQueries(board, List.of()), Color.BLACK));
    }

    @Test
    void ai_alwaysPicksOneOfTheLegalMoves() {
        new StandardChessSetup(new PieceFactory(List.of(
                new PawnDefinition(), new RookDefinition(), new KnightDefinition(),
                new BishopDefinition(), new QueenDefinition(), new KingDefinition()))).setup(board);
        MoveValidator validator = new MoveValidator(checkDetector, new MoveCommandFactory(List.of()));
        List<Move> legal = validator.getLegalMoves(board, new MoveHistory(), Color.BLACK);

        for (int seed = 0; seed < 10; seed++) {
            Move chosen = new AIPlayerStrategy(evaluator, new Random(seed))
                    .chooseMove(new FakeGameQueries(board, legal), Color.BLACK);
            assertTrue(legal.contains(chosen));
        }
    }

    @Test
    void humanVersusAi_fullTurnRunsInMemory() {
        new StandardChessSetup(new PieceFactory(List.of(
                new PawnDefinition(), new RookDefinition(), new KnightDefinition(),
                new BishopDefinition(), new QueenDefinition(), new KingDefinition()))).setup(board);
        MoveCommandFactory commandFactory = new MoveCommandFactory(List.of());
        MoveValidator validator = new MoveValidator(checkDetector, commandFactory);
        TurnManager turns = new TurnManager(
                new Player(Color.WHITE, new HumanPlayerStrategy(new FakeMoveInput(List.of(move("e2", "e4"))))),
                new Player(Color.BLACK, ai()));
        Game game = new Game(board, turns, validator, new GameStatusEvaluator(checkDetector, List.of()),
                commandFactory, new MoveHistory());
        game.start();

        assertTrue(game.playTurn(), "human plays e2-e4");
        assertTrue(game.playTurn(), "AI answers with a legal move");

        assertEquals(Color.WHITE, game.getCurrentTurn());
        assertEquals(16, game.getBoard().getPositionsOf(Color.BLACK).size());
    }
}
