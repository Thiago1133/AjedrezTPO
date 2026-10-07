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
import ar.edu.uade.chess.core.piece.Piece;
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
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

/**
 * Composition root: the only place where concrete classes are instantiated and wired
 * together (dependency injection by hand), for both adapters (Swing window and console).
 * Extending the game (a new piece, rule or board size) means changing the wiring here,
 * not the core classes.
 */
public class Main {
    private static final String CONSOLE_FLAG = "--consola";
    /** Console answers to "1 o 2": looked up in a map, never compared as text. */
    private static final Map<String, Boolean> VERSUS_COMPUTER_ANSWERS = Map.of("1", false, "2", true);

    public static void main(String[] args) {
        // Pieces and promotion have no per-game state, so every game shares them.
        PieceFactory pieceFactory = new PieceFactory(List.of(
                new PawnDefinition(), new RookDefinition(), new KnightDefinition(),
                new BishopDefinition(), new QueenDefinition(), new KingDefinition()));
        PromotionRule promotionRule = new PromotionRule(pieceFactory);

        Runnable window = () -> SwingUtilities.invokeLater(() -> {
            ChessWindow chessWindow = new ChessWindow(
                    versusComputer -> createGame(pieceFactory, promotionRule, versusComputer, color -> null),
                    previewPieces(), promotionRule);
            if (chessWindow.hasGame()) chessWindow.setVisible(true);
        });
        // Launch flags are looked up like console commands; without a known flag the window opens.
        Map<String, Runnable> launchers = Map.of(CONSOLE_FLAG, () -> runConsole(pieceFactory, promotionRule));
        Arrays.stream(args).map(launchers::get).filter(Objects::nonNull).findFirst().orElse(window).run();
    }

    private static void runConsole(PieceFactory pieceFactory, PromotionRule promotionRule) {
        Scanner scanner = new Scanner(System.in);
        ConsoleUI console = new ConsoleUI(scanner, promotionRule);
        console.run(createGame(pieceFactory, promotionRule, askVersusComputer(scanner), console));
    }

    /** Builds a game with human input supplied by the selected adapter. */
    private static Game createGame(PieceFactory pieceFactory, PromotionRule promotionRule,
                                   boolean versusComputer, MoveInput humanInput) {
        Board board = new Board(8, 8);
        new StandardChessSetup(pieceFactory).setup(board);

        CheckDetector checkDetector = new CheckDetector();
        MoveCommandFactory commandFactory = new MoveCommandFactory(List.of(
                new CastlingRule(checkDetector),
                new EnPassantRule(),
                promotionRule));
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

    /** Pieces shown in the new-game dialog to preview each piece style and board theme. */
    private static List<Piece> previewPieces() {
        return List.of(new KingDefinition().create(Color.WHITE), new QueenDefinition().create(Color.BLACK),
                new KnightDefinition().create(Color.WHITE), new PawnDefinition().create(Color.BLACK));
    }

    /** The computer plays black when the console user picks option 2. */
    private static boolean askVersusComputer(Scanner scanner) {
        System.out.println("Modo de juego:\n  1) Dos jugadores\n  2) Contra la computadora (jugás con blancas)");
        System.out.print("Elegí 1 o 2 > ");
        return scanner.hasNextLine() && VERSUS_COMPUTER_ANSWERS.getOrDefault(scanner.nextLine().trim(), false);
    }
}
