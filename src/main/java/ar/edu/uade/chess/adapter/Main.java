package ar.edu.uade.chess.adapter;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import ar.edu.uade.chess.adapter.engine.StockfishEngine;
import ar.edu.uade.chess.adapter.gui.ChessWindow;
import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.command.MoveCommandFactory;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.game.AIPlayerStrategy;
import ar.edu.uade.chess.core.game.ChessClock;
import ar.edu.uade.chess.core.game.Difficulty;
import ar.edu.uade.chess.core.game.EasyLevel;
import ar.edu.uade.chess.core.game.EngineLevel;
import ar.edu.uade.chess.core.game.EnginePlayerStrategy;
import ar.edu.uade.chess.core.game.Game;
import ar.edu.uade.chess.core.game.HardLevel;
import ar.edu.uade.chess.core.game.HumanPlayerStrategy;
import ar.edu.uade.chess.core.game.MaterialEvaluator;
import ar.edu.uade.chess.core.game.MediumLevel;
import ar.edu.uade.chess.core.game.Player;
import ar.edu.uade.chess.core.game.PlayerStrategy;
import ar.edu.uade.chess.core.game.TimeControl;
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
import ar.edu.uade.chess.core.port.out.ChessEngine;
import ar.edu.uade.chess.core.port.out.MoveInput;
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

/**
 * Composition root: the only place where concrete classes are instantiated and wired
 * together (dependency injection by hand), for both adapters (Swing window and console).
 */
public class Main {
    private static final String CONSOLE_FLAG = "--consola";
    /** Console answers, looked up in maps, never compared as text. */
    private static final Map<String, Boolean> VERSUS_COMPUTER_ANSWERS = Map.of("1", false, "2", true);
    /**
     * Share of random moves that bring Stockfish (1320 Elo at its weakest) down to the levels
     * below it: about 800 Elo for medium and less for easy. Measured in games against Stockfish.
     */
    private static final double EASY_RANDOM_MOVES = 1.0;
    private static final double MEDIUM_RANDOM_MOVES = 0.40;
    private static final Map<String, Difficulty> DIFFICULTY_ANSWERS = Map.of(
            "1", Difficulty.EASY, "2", Difficulty.MEDIUM, "3", Difficulty.HARD);

    public static void main(String[] args) {
        // Pieces and promotion have no per-game state, so every game shares them.
        PieceFactory pieceFactory = new PieceFactory(List.of(
                new PawnDefinition(), new RookDefinition(), new KnightDefinition(),
                new BishopDefinition(), new QueenDefinition(), new KingDefinition()));
        PromotionRule promotionRule = new PromotionRule(pieceFactory);

        // One engine process for the whole session, started early and closed when the program exits.
        StockfishEngine engine = new StockfishEngine(StockfishEngine.findBundledExecutable().orElse(null), promotionRule);
        engine.startInBackground();
        Runtime.getRuntime().addShutdownHook(new Thread(engine::close));
        Map<Difficulty, EngineLevel> levels = new EnumMap<>(Map.of(
                Difficulty.EASY, new EasyLevel(new Random(), EASY_RANDOM_MOVES),
                Difficulty.MEDIUM, new MediumLevel(new Random(), MEDIUM_RANDOM_MOVES),
                Difficulty.HARD, new HardLevel()));

        Runnable window = () -> SwingUtilities.invokeLater(() -> {
            ChessWindow chessWindow = new ChessWindow(
                    settings -> createGame(pieceFactory, promotionRule, engine, levels, settings, color -> null),
                    previewPieces(), promotionRule, engine.isAvailable());
            if (chessWindow.hasGame()) chessWindow.setVisible(true);
        });
        // Launch flags are looked up like console commands; without a known flag the window opens.
        Map<String, Runnable> launchers = Map.of(CONSOLE_FLAG,
                () -> runConsole(pieceFactory, promotionRule, engine, levels));
        Arrays.stream(args).map(launchers::get).filter(Objects::nonNull).findFirst().orElse(window).run();
    }

    private static void runConsole(PieceFactory pieceFactory, PromotionRule promotionRule,
                                   StockfishEngine engine, Map<Difficulty, EngineLevel> levels) {
        Scanner scanner = new Scanner(System.in);
        ConsoleUI console = new ConsoleUI(scanner, promotionRule);
        boolean versusComputer = askVersusComputer(scanner);
        Difficulty difficulty = versusComputer ? askDifficulty(scanner, engine.isAvailable()) : Difficulty.MEDIUM;
        GameSettings settings = new GameSettings(versusComputer, TimeControl.UNLIMITED, difficulty);
        console.run(createGame(pieceFactory, promotionRule, engine, levels, settings, console));
    }

    /** Builds a game with human input supplied by the selected adapter. */
    private static Game createGame(PieceFactory pieceFactory, PromotionRule promotionRule, ChessEngine engine,
                                   Map<Difficulty, EngineLevel> levels, GameSettings settings,
                                   MoveInput humanInput) {
        boolean versusComputer = settings.versusComputer();
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

        // The computer asks the engine; when the engine cannot answer, the built-in AI moves.
        PlayerStrategy builtInAi = new AIPlayerStrategy(new MaterialEvaluator(checkDetector), new Random());
        PlayerStrategy blackStrategy = versusComputer
                ? new EnginePlayerStrategy(engine, levels.get(settings.difficulty()), builtInAi)
                : new HumanPlayerStrategy(humanInput);
        TurnManager turnManager = new TurnManager(
                new Player(Color.WHITE, new HumanPlayerStrategy(humanInput)),
                new Player(Color.BLACK, blackStrategy));

        // Against the computer there is no clock: the time limit only applies between two humans.
        ChessClock clock = new ChessClock(versusComputer ? TimeControl.UNLIMITED : settings.timeControl());
        return new Game(board, turnManager, moveValidator, statusEvaluator, commandFactory, new MoveHistory(), clock);
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

    private static Difficulty askDifficulty(Scanner scanner, boolean engineAvailable) {
        System.out.println("Dificultad:\n  1) Fácil\n  2) Medio\n  3) Difícil");
        if (!engineAvailable) {
            System.out.println("(Stockfish no está disponible: la computadora juega sin él.)");
        }
        System.out.print("Elegí 1, 2 o 3 > ");
        return scanner.hasNextLine()
                ? DIFFICULTY_ANSWERS.getOrDefault(scanner.nextLine().trim(), Difficulty.MEDIUM)
                : Difficulty.MEDIUM;
    }
}
