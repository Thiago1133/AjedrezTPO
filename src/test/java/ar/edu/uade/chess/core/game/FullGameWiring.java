package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.command.MoveCommandFactory;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.piece.BishopDefinition;
import ar.edu.uade.chess.core.piece.KingDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.rules.CastlingRule;
import ar.edu.uade.chess.core.rules.CheckDetector;
import ar.edu.uade.chess.core.rules.EnPassantRule;
import ar.edu.uade.chess.core.rules.MoveValidator;
import ar.edu.uade.chess.core.rules.PromotionRule;
import ar.edu.uade.chess.core.status.CheckmateCondition;
import ar.edu.uade.chess.core.status.FiftyMoveRuleCondition;
import ar.edu.uade.chess.core.status.GameStatusEvaluator;
import ar.edu.uade.chess.core.status.InsufficientMaterialCondition;
import ar.edu.uade.chess.core.status.StalemateCondition;
import ar.edu.uade.chess.core.status.ThreefoldRepetitionCondition;

import java.util.List;
import java.util.Random;

/**
 * Same wiring as the composition root (Main), so these tests exercise every real rule and
 * end condition.
 */
public final class FullGameWiring {
    private FullGameWiring() {
    }

    public static PieceFactory standardPieces() {
        return new PieceFactory(List.of(
                new PawnDefinition(), new RookDefinition(), new KnightDefinition(),
                new BishopDefinition(), new QueenDefinition(), new KingDefinition()));
    }

    /** Both sides are moved by the test through Game.move. */
    public static Game twoPlayers(Board board, PieceFactory pieceFactory) {
        return twoPlayers(board, pieceFactory, TimeControl.UNLIMITED);
    }

    /** Two players with a clock, as chosen in the new-game dialog. */
    public static Game twoPlayers(Board board, PieceFactory pieceFactory, TimeControl timeControl) {
        FakeMoveInput noInput = new FakeMoveInput(List.of());
        return create(board, pieceFactory, new HumanPlayerStrategy(noInput), timeControl);
    }

    /** Black is played by the real AI, as in "Contra la computadora". */
    static Game versusComputer(Board board, PieceFactory pieceFactory) {
        return create(board, pieceFactory,
                new AIPlayerStrategy(new MaterialEvaluator(new CheckDetector()), new Random(0)),
                TimeControl.UNLIMITED);
    }

    private static Game create(Board board, PieceFactory pieceFactory, PlayerStrategy blackStrategy,
                               TimeControl timeControl) {
        CheckDetector checkDetector = new CheckDetector();
        MoveCommandFactory commandFactory = new MoveCommandFactory(List.of(
                new CastlingRule(checkDetector),
                new EnPassantRule(),
                new PromotionRule(pieceFactory)));
        MoveValidator moveValidator = new MoveValidator(checkDetector, commandFactory);
        GameStatusEvaluator statusEvaluator = new GameStatusEvaluator(checkDetector, List.of(
                new CheckmateCondition(moveValidator, checkDetector),
                new StalemateCondition(moveValidator, checkDetector),
                new InsufficientMaterialCondition(),
                new ThreefoldRepetitionCondition(),
                new FiftyMoveRuleCondition()));
        TurnManager turns = new TurnManager(
                new Player(Color.WHITE, new HumanPlayerStrategy(new FakeMoveInput(List.of()))),
                new Player(Color.BLACK, blackStrategy));
        return new Game(board, turns, moveValidator, statusEvaluator, commandFactory, new MoveHistory(),
                new ChessClock(timeControl));
    }
}
