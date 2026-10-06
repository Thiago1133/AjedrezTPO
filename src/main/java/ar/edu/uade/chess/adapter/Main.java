package ar.edu.uade.chess.adapter;

import ar.edu.uade.chess.adapter.gui.ChessWindow;
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
import ar.edu.uade.chess.core.port.MoveInput;
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

import javax.swing.SwingUtilities;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/** Composition root for the console and Swing adapters. */
public class Main {
    private static final String CONSOLE_FLAG = "--consola";
    private static final String VERSUS_COMPUTER_OPTION = "2";

    public static void main(String[] args) {
        if (Arrays.asList(args).contains(CONSOLE_FLAG)) {
            runConsole();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            ChessWindow window = new ChessWindow(
                    versusComputer -> createGame(versusComputer, color -> null));
            if (window.hasGame()) window.setVisible(true);
        });
    }

    private static void runConsole() {
        Scanner scanner = new Scanner(System.in);
        ConsoleUI console = new ConsoleUI(scanner);
        console.run(createGame(askVersusComputer(scanner), console));
    }

    /** Builds a game with human input supplied by the selected adapter. */
    private static Game createGame(boolean versusComputer, MoveInput humanInput) {
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
                new InsufficientMaterialCondition(),
                new ThreefoldRepetitionCondition(),
                new FiftyMoveRuleCondition()));

        PlayerStrategy blackStrategy = versusComputer
                ? new AIPlayerStrategy(new MaterialEvaluationStrategy(checkDetector), new Random())
                : new HumanPlayerStrategy(humanInput);
        TurnManager turnManager = new TurnManager(
                new Player(Color.WHITE, new HumanPlayerStrategy(humanInput)),
                new Player(Color.BLACK, blackStrategy));

        return new Game(board, turnManager, moveValidator, statusEvaluator, commandFactory, new MoveHistory());
    }

    /** The computer plays black when the console user picks option 2. */
    private static boolean askVersusComputer(Scanner scanner) {
        System.out.println("Modo de juego:\n  1) Dos jugadores\n  2) Contra la computadora (jugás con blancas)");
        System.out.print("Elegí 1 o 2 > ");
        return scanner.hasNextLine() && scanner.nextLine().trim().equals(VERSUS_COMPUTER_OPTION);
    }
}
