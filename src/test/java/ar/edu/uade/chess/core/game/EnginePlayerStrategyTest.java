package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.port.in.GameQueries;
import ar.edu.uade.chess.core.port.out.ChessEngine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The computer player with each difficulty, against a stub engine that proposes fixed
 * moves. The game is real, so captures and checks are judged by the real rules.
 */
class EnginePlayerStrategyTest {
    private final PieceFactory pieceFactory = FullGameWiring.standardPieces();
    private final Board board = new Board(8, 8);

    /** Stub: always proposes the given moves and remembers the difficulty it was asked for. */
    private static final class StubEngine implements ChessEngine {
        private final List<Move> candidates;
        private final List<Difficulty> asked = new ArrayList<>();

        StubEngine(Move... candidates) {
            this.candidates = List.of(candidates);
        }

        @Override
        public List<Move> candidateMoves(GameQueries game, Difficulty difficulty) {
            asked.add(difficulty);
            return candidates;
        }
    }

    private static Position at(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    private static Move move(String from, String to) {
        return new Move(at(from), at(to));
    }

    private void place(String id, Color color, String square) {
        board.placePiece(pieceFactory.create(id, color), at(square));
    }

    /** White to move: a rook that can make a quiet move or take the pawn on a5. */
    private Game start() {
        place("pawn", Color.BLACK, "a5");
        return startWithoutCaptures();
    }

    /** White to move: a king and a rook with only quiet moves available. */
    private Game startWithoutCaptures() {
        place("king", Color.WHITE, "e1");
        place("rook", Color.WHITE, "a1");
        place("king", Color.BLACK, "e8");
        Game game = FullGameWiring.twoPlayers(board, pieceFactory);
        game.start();
        return game;
    }

    private static EnginePlayerStrategy player(ChessEngine engine, EngineLevel level) {
        PlayerStrategy fallback = (game, color) -> move("e1", "f1");
        return new EnginePlayerStrategy(engine, level, fallback);
    }

    @Test
    void hard_playsTheEnginesBestMove() {
        Game game = start();
        StubEngine engine = new StubEngine(move("a1", "a2"), move("a1", "a5"));

        Move chosen = player(engine, new HardLevel()).chooseMove(game, Color.WHITE);

        assertEquals(move("a1", "a2"), chosen);
        assertEquals(List.of(Difficulty.HARD), engine.asked, "the engine is asked at the level's difficulty");
    }

    @Test
    void medium_givesCheckWhenItCan() {
        place("king", Color.WHITE, "e1");
        place("rook", Color.WHITE, "a1");
        place("king", Color.BLACK, "h8");
        place("pawn", Color.BLACK, "h7");
        Game game = FullGameWiring.twoPlayers(board, pieceFactory);
        game.start();
        StubEngine engine = new StubEngine(move("a1", "a2"), move("a1", "a8"));

        Move chosen = player(engine, new MediumLevel(new Random(0), 0)).chooseMove(game, Color.WHITE);

        assertEquals(move("a1", "a8"), chosen, "a1-a8 checks the king on h8");
    }

    @Test
    void medium_withoutAnyCheck_playsTheEnginesBestMove() {
        Game game = start();
        StubEngine engine = new StubEngine(move("a1", "a2"), move("a1", "a3"));

        Move chosen = player(engine, new MediumLevel(new Random(0), 0)).chooseMove(game, Color.WHITE);

        assertEquals(move("a1", "a2"), chosen);
    }

    @Test
    void easy_capturesWhenItCan_whateverTheRandomPick() {
        Game game = start();
        StubEngine engine = new StubEngine(move("a1", "a2"), move("a1", "a5"), move("a1", "a3"));

        for (int seed = 0; seed < 10; seed++) {
            Move chosen = player(engine, new EasyLevel(new Random(seed), 0)).chooseMove(game, Color.WHITE);
            assertEquals(move("a1", "a5"), chosen, "seed " + seed);
        }
    }

    /** The engine may leave a capture out of its suggestions; the easy level still takes it. */
    @Test
    void easy_capturesEvenWhenTheEngineDoesNotSuggestIt() {
        Game game = start();
        StubEngine engine = new StubEngine(move("a1", "a2"), move("a1", "a3"));

        Move chosen = player(engine, new EasyLevel(new Random(3), 1)).chooseMove(game, Color.WHITE);

        assertEquals(move("a1", "a5"), chosen, "the rook takes the pawn on a5");
    }

    @Test
    void easy_withoutCaptures_picksARandomCandidate_repeatablyForTheSameSeed() {
        Game game = startWithoutCaptures();
        List<Move> quiet = List.of(move("a1", "a2"), move("a1", "a3"), move("a1", "a4"));
        StubEngine engine = new StubEngine(quiet.toArray(Move[]::new));

        Move first = player(engine, new EasyLevel(new Random(7), 0)).chooseMove(game, Color.WHITE);
        Move again = player(engine, new EasyLevel(new Random(7), 0)).chooseMove(game, Color.WHITE);

        assertTrue(quiet.contains(first));
        assertEquals(first, again);
    }

    /** Medium plays some random moves, but never instead of a check. */
    @Test
    void medium_randomMoves_neverReplaceACheck() {
        place("king", Color.WHITE, "e1");
        place("rook", Color.WHITE, "a1");
        place("king", Color.BLACK, "h8");
        place("pawn", Color.BLACK, "h7");
        Game game = FullGameWiring.twoPlayers(board, pieceFactory);
        game.start();
        StubEngine engine = new StubEngine(move("a1", "a2"), move("a1", "a8"));

        Move chosen = player(engine, new MediumLevel(new Random(0), 1)).chooseMove(game, Color.WHITE);

        assertEquals(move("a1", "a8"), chosen);
    }

    /** With only random moves, medium and easy may play any legal move, not just the engine's. */
    @Test
    void randomMoves_comeFromAllLegalMoves() {
        Game game = startWithoutCaptures();
        StubEngine engine = new StubEngine(move("a1", "a2"));
        List<Move> legal = game.getLegalMoves();
        boolean outsideTheEngine = false;

        for (int seed = 0; seed < 20; seed++) {
            Move medium = player(engine, new MediumLevel(new Random(seed), 1)).chooseMove(game, Color.WHITE);
            Move easy = player(engine, new EasyLevel(new Random(seed), 1)).chooseMove(game, Color.WHITE);
            assertTrue(legal.contains(medium) && legal.contains(easy));
            outsideTheEngine |= !medium.equals(move("a1", "a2")) || !easy.equals(move("a1", "a2"));
        }

        assertTrue(outsideTheEngine, "some random moves differ from the engine's single suggestion");
    }

    @Test
    void illegalSuggestions_areIgnored() {
        Game game = start();
        StubEngine engine = new StubEngine(move("e1", "e3"), move("a1", "h1"), move("a1", "a2"));

        Move chosen = player(engine, new HardLevel()).chooseMove(game, Color.WHITE);

        assertEquals(move("a1", "a2"), chosen, "a king cannot move two squares and a1-h1 jumps the king");
    }

    @Test
    void withoutAnAnswerFromTheEngine_theFallbackMoves() {
        Game game = start();
        StubEngine silent = new StubEngine();

        Move chosen = player(silent, new HardLevel()).chooseMove(game, Color.WHITE);

        assertEquals(move("e1", "f1"), chosen);
    }

    @Test
    void theEnginePlayer_isAutomatic() {
        assertTrue(player(new StubEngine(), new HardLevel()).isAutomatic(), "undo skips the computer's turns");
    }
}
