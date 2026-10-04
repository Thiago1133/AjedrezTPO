package ar.edu.uade.chess.adapter;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.command.MoveCommandFactory;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.game.AIPlayerStrategy;
import ar.edu.uade.chess.core.game.Game;
import ar.edu.uade.chess.core.game.HumanPlayerStrategy;
import ar.edu.uade.chess.core.game.MaterialEvaluationStrategy;
import ar.edu.uade.chess.core.game.Player;
import ar.edu.uade.chess.core.game.PlayerStrategy;
import ar.edu.uade.chess.core.game.TurnManager;
import ar.edu.uade.chess.core.piece.BishopDefinition;
import ar.edu.uade.chess.core.piece.KingDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.piece.StandardChessSetup;
import ar.edu.uade.chess.core.rules.CastlingRule;
import ar.edu.uade.chess.core.rules.CheckDetector;
import ar.edu.uade.chess.core.rules.EnPassantRule;
import ar.edu.uade.chess.core.rules.MoveValidator;
import ar.edu.uade.chess.core.rules.PromotionRule;
import ar.edu.uade.chess.core.status.CheckmateCondition;
import ar.edu.uade.chess.core.status.FiftyMoveRuleCondition;
import ar.edu.uade.chess.core.status.GameStatusEvaluator;
import ar.edu.uade.chess.core.status.StalemateCondition;
import ar.edu.uade.chess.core.status.ThreefoldRepetitionCondition;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Composition root: the only place where concrete classes are instantiated and
 * wired together (dependency injection by hand). Extending the game means
 * changing the wiring here, not the core classes.
 */
public class Main {

    public static void main(String[] args) {
        PieceFactory pieceFactory = new PieceFactory(List.of(
                new PawnDefinition(), new RookDefinition(), new KnightDefinition(),
                new BishopDefinition(), new QueenDefinition(), new KingDefinition()));
        Board board = new Board(8, 8);
        new StandardChessSetup(pieceFactory).setup(board);

        CheckDetector checkDetector = new CheckDetector();
        MoveCommandFactory commandFactory = new MoveCommandFactory(List.of(
                new CastlingRule(checkDetector),
                new EnPassantRule(),
                new PromotionRule(pieceFactory)));
        MoveValidator moveValidator = new MoveValidator(checkDetector, commandFactory);
        GameStatusEvaluator statusEvaluator = new GameStatusEvaluator(checkDetector, List.of(
                new CheckmateCondition(moveValidator, checkDetector),
                new StalemateCondition(moveValidator, checkDetector),
                new ThreefoldRepetitionCondition(),
                new FiftyMoveRuleCondition()));

        Scanner scanner = new Scanner(System.in);
        ConsoleUI console = new ConsoleUI(scanner);
        PlayerStrategy blackStrategy = askVersusComputer(scanner)
                ? new AIPlayerStrategy(new MaterialEvaluationStrategy(checkDetector), new Random())
                : new HumanPlayerStrategy(console);
        TurnManager turnManager = new TurnManager(
                new Player(Color.WHITE, new HumanPlayerStrategy(console)),
                new Player(Color.BLACK, blackStrategy));

        Game game = new Game(board, turnManager, moveValidator, statusEvaluator, commandFactory, new MoveHistory());
        console.run(game);
    }

    /** Game mode: the computer plays black when the user picks option 2. */
    private static boolean askVersusComputer(Scanner scanner) {
        System.out.println("Modo de juego:\n  1) Dos jugadores\n  2) Contra la computadora (jugás con blancas)");
        System.out.print("Elegí 1 o 2 > ");
        return scanner.hasNextLine() && scanner.nextLine().trim().equals("2");
    }
}
