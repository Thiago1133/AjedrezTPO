package ar.edu.uade.chess.adapter;

import ar.edu.uade.chess.adapter.gui.ChessWindow;
import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.command.MoveCommandFactory;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.game.Game;
import ar.edu.uade.chess.core.game.AIPlayerStrategy;
import ar.edu.uade.chess.core.game.HumanPlayerStrategy;
import ar.edu.uade.chess.core.game.MaterialEvaluationStrategy;
import ar.edu.uade.chess.core.game.Player;
import ar.edu.uade.chess.core.game.TurnManager;
import ar.edu.uade.chess.core.piece.*;
import ar.edu.uade.chess.core.rules.*;
import ar.edu.uade.chess.core.status.*;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Random;

/** Application composition root. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChessWindow window = new ChessWindow(Main::createGame);
            if (window.hasGame()) window.setVisible(true);
        });
    }

    private static Game createGame(boolean versusComputer) {
        PieceFactory pieceFactory = new PieceFactory(List.of(new PawnDefinition(), new RookDefinition(),
                new KnightDefinition(), new BishopDefinition(), new QueenDefinition(), new KingDefinition()));
        Board board = new Board(8, 8);
        new StandardChessSetup(pieceFactory).setup(board);
        CheckDetector checkDetector = new CheckDetector();
        MoveCommandFactory commandFactory = new MoveCommandFactory(List.of(new CastlingRule(checkDetector),
                new EnPassantRule(), new PromotionRule(pieceFactory)));
        MoveValidator moveValidator = new MoveValidator(checkDetector, commandFactory);
        GameStatusEvaluator statusEvaluator = new GameStatusEvaluator(checkDetector, List.of(
                new CheckmateCondition(moveValidator, checkDetector), new StalemateCondition(moveValidator, checkDetector),
                new InsufficientMaterialCondition(), new ThreefoldRepetitionCondition(),
                new FiftyMoveRuleCondition()));
        // GUI players do not request moves through the console; the window calls Game.move directly.
        HumanPlayerStrategy idle = new HumanPlayerStrategy(color -> null);
        var black = versusComputer
                ? new AIPlayerStrategy(new MaterialEvaluationStrategy(checkDetector), new Random())
                : idle;
        TurnManager turnManager = new TurnManager(new Player(Color.WHITE, idle), new Player(Color.BLACK, black));
        return new Game(board, turnManager, moveValidator, statusEvaluator, commandFactory, new MoveHistory());
    }
}
