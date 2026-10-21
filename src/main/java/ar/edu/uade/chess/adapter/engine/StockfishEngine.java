package ar.edu.uade.chess.adapter.engine;

import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.game.Difficulty;
import ar.edu.uade.chess.core.port.in.GameQueries;
import ar.edu.uade.chess.core.port.in.PromotionOptions;
import ar.edu.uade.chess.core.port.out.ChessEngine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The Stockfish engine as the game's chess engine (UCI protocol). How strong each
 * difficulty plays is set here, with Stockfish's own options. Any problem (no executable,
 * a position it cannot read, no answer in time) gives no candidates, so the computer's
 * fallback player moves instead.
 */
public final class StockfishEngine implements ChessEngine, AutoCloseable {
    /**
     * How Stockfish plays at a difficulty: an exact rating (UCI_Elo, 1320 at the lowest) or, below
     * that, its weakest skill level and a one-move search; plus the candidate lines and time limit.
     */
    private record Settings(int elo, int skillLevel, int depth, int candidates, Duration moveTime) {
        static final int NO_ELO = 0;
        static final int NO_DEPTH_LIMIT = 0;
    }

    /** Easy and medium go below Stockfish's lowest rating; their levels add random moves on top. */
    private static final Map<Difficulty, Settings> SETTINGS = new EnumMap<>(Map.of(
            Difficulty.EASY, new Settings(Settings.NO_ELO, 0, 1, 10, Duration.ofMillis(100)),
            Difficulty.MEDIUM, new Settings(Settings.NO_ELO, 0, 1, 5, Duration.ofMillis(100)),
            Difficulty.HARD, new Settings(1500, 20, Settings.NO_DEPTH_LIMIT, 1, Duration.ofMillis(300))));

    /** Extra time on top of the search time before giving up on an answer. */
    private static final Duration GRACE = Duration.ofSeconds(2);
    private static final Duration STARTUP = Duration.ofSeconds(5);
    private static final Pattern UCI_OK = Pattern.compile("^uciok");
    private static final Pattern READY_OK = Pattern.compile("^readyok");
    private static final Pattern BEST_MOVE = Pattern.compile("^bestmove");
    /** Where Maven unpacks Stockfish, next to the game's jar or classes. */
    private static final String ENGINE_FOLDER = "engine";
    /** Windows build first; on Linux and macOS the engine file has no extension but can be run. */
    private static final PathMatcher WINDOWS_EXECUTABLE = FileSystems.getDefault().getPathMatcher("glob:**/stockfish*.exe");
    private static final PathMatcher ENGINE_FILE = FileSystems.getDefault().getPathMatcher("glob:**/stockfish*");

    private final Path executable;
    private final PromotionOptions promotionOptions;
    private UciProcess process;

    public StockfishEngine(Path executable, PromotionOptions promotionOptions) {
        this.executable = executable;
        this.promotionOptions = promotionOptions;
    }

    /** The Stockfish executable that the build left next to the game, if there is one. */
    public static Optional<Path> findBundledExecutable() {
        try {
            Path codeLocation = Path.of(StockfishEngine.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path folder = codeLocation.getParent().resolve(ENGINE_FOLDER);
            if (!Files.isDirectory(folder)) return Optional.empty();
            List<Path> files;
            try (Stream<Path> walk = Files.walk(folder)) {
                files = walk.filter(Files::isRegularFile).sorted().toList();
            }
            return files.stream().filter(WINDOWS_EXECUTABLE::matches).findFirst()
                    .or(() -> files.stream().filter(ENGINE_FILE::matches).filter(Files::isExecutable).findFirst());
        } catch (IOException | URISyntaxException | RuntimeException notFound) {
            return Optional.empty();
        }
    }

    public boolean isAvailable() {
        return executable != null && Files.isRegularFile(executable);
    }

    /**
     * Starts the engine in the background, so its first answer is not delayed by loading it
     * (about a second). The background thread only starts the engine; it never touches the game.
     */
    public void startInBackground() {
        if (!isAvailable()) return;
        Thread starter = new Thread(() -> {
            try {
                running();
            } catch (IOException | TimeoutException | RuntimeException failure) {
                stop();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }, "uci-engine-start");
        starter.setDaemon(true);
        starter.start();
    }

    @Override
    public synchronized List<Move> candidateMoves(GameQueries game, Difficulty difficulty) {
        Optional<String> fen = FenWriter.write(game);
        if (fen.isEmpty() || !isAvailable()) return List.of();
        Settings settings = SETTINGS.get(difficulty);
        try {
            UciProcess engine = running();
            boolean rated = settings.elo() != Settings.NO_ELO;
            engine.send("setoption name UCI_LimitStrength value " + rated);
            if (rated) engine.send("setoption name UCI_Elo value " + settings.elo());
            engine.send("setoption name Skill Level value " + settings.skillLevel());
            engine.send("setoption name MultiPV value " + settings.candidates());
            engine.send("position fen " + fen.get());
            String depth = settings.depth() == Settings.NO_DEPTH_LIMIT ? "" : "depth " + settings.depth() + " ";
            engine.send("go " + depth + "movetime " + settings.moveTime().toMillis());
            List<String> output = engine.readUntil(BEST_MOVE, settings.moveTime().plus(GRACE));
            return UciMoves.candidates(output, promotionOptions.getPromotionChoices(game.getCurrentTurn()));
        } catch (IOException | TimeoutException | RuntimeException failure) {
            stop();
            return List.of();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            stop();
            return List.of();
        }
    }

    /** Starts the engine the first time it is needed, or again if it stopped. */
    private synchronized UciProcess running() throws IOException, TimeoutException, InterruptedException {
        if (process != null && process.isAlive()) return process;
        process = UciProcess.start(executable);
        process.send("uci");
        process.readUntil(UCI_OK, STARTUP);
        process.send("isready");
        process.readUntil(READY_OK, STARTUP);
        return process;
    }

    private synchronized void stop() {
        if (process != null) {
            process.close();
            process = null;
        }
    }

    @Override
    public void close() {
        stop();
    }
}
