package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.BoardSetup;
import ar.edu.uade.chess.core.piece.JumpMovement;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceDefinition;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.SlidingMovement;
import ar.edu.uade.chess.core.piece.StandardChessSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Rehearsal of the live-defense extension "El Cacique": a new piece that combines the
 * knight's jump with an orthogonal slide of up to two squares. It is added only with new
 * classes (a definition and a setup) plus registration in the factory; no existing class
 * changes, and it plays through the real Game with every rule and end condition.
 */
class CaciqueExtensionTest {

    /** The new piece: composed from existing movement capabilities. */
    static final class CaciqueDefinition implements PieceDefinition {
        private static final List<Direction> JUMPS = List.of(
                new Direction(2, 1), new Direction(2, -1), new Direction(-2, 1), new Direction(-2, -1),
                new Direction(1, 2), new Direction(1, -2), new Direction(-1, 2), new Direction(-1, -2));

        @Override
        public String getId() {
            return "cacique";
        }

        @Override
        public Piece create(Color color) {
            return new Piece(getId(), color == Color.WHITE ? 'C' : 'c', color, 6, Set.of(),
                    List.of(new JumpMovement(JUMPS), new SlidingMovement(Direction.ORTHOGONAL, 2)));
        }
    }

    /** New variant setup: the standard position with caciques instead of the queen-side knights. */
    static final class CaciqueChessSetup implements BoardSetup {
        private final BoardSetup standard;
        private final PieceFactory pieceFactory;

        CaciqueChessSetup(BoardSetup standard, PieceFactory pieceFactory) {
            this.standard = standard;
            this.pieceFactory = pieceFactory;
        }

        @Override
        public void setup(Board board) {
            standard.setup(board);
            board.removePiece(at("b1"));
            board.placePiece(pieceFactory.create("cacique", Color.WHITE), at("b1"));
            board.removePiece(at("b8"));
            board.placePiece(pieceFactory.create("cacique", Color.BLACK), at("b8"));
        }
    }

    private final SpyGameObserver spy = new SpyGameObserver();
    private PieceFactory pieceFactory;
    private Board board;

    private static Position at(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    private static Move move(String from, String to) {
        return new Move(at(from), at(to));
    }

    @BeforeEach
    void registerTheCacique() {
        pieceFactory = FullGameWiring.standardPieces();
        pieceFactory.register(new CaciqueDefinition());
        board = new Board(8, 8);
    }

    private void place(String id, Color color, String square) {
        board.placePiece(pieceFactory.create(id, color), at(square));
    }

    private Game start(Game game) {
        game.addObserver(spy);
        game.start();
        return game;
    }

    private Set<Position> legalTargetsFrom(Game game, String square) {
        return game.getLegalMoves().stream()
                .filter(candidate -> candidate.getFrom().equals(at(square)))
                .map(Move::getTo)
                .collect(Collectors.toSet());
    }

    @Test
    void startingPosition_withCaciques_onlyAllowsItsFreeJumps() {
        new CaciqueChessSetup(new StandardChessSetup(pieceFactory), pieceFactory).setup(board);
        Game game = start(FullGameWiring.twoPlayers(board, pieceFactory));

        assertEquals("cacique", board.getPiece(at("b8")).getId());
        assertEquals(Set.of(at("a3"), at("c3")), legalTargetsFrom(game, "b1"),
                "slides are blocked by its own pieces; jumps are not");
    }

    @Test
    void legalMoves_comeFromItsComposedRules() {
        place("king", Color.WHITE, "a1");
        place("king", Color.BLACK, "h8");
        place("cacique", Color.WHITE, "d4");
        Game game = start(FullGameWiring.twoPlayers(board, pieceFactory));

        Set<Position> targets = legalTargetsFrom(game, "d4");

        assertEquals(16, targets.size(), "8 jumps + 8 orthogonal squares");
        assertTrue(targets.containsAll(Set.of(at("e6"), at("c2"), at("d6"), at("b4"))));
        assertFalse(targets.contains(at("d7")), "slides at most two squares");
        assertFalse(targets.contains(at("e5")), "no diagonal step");
    }

    @Test
    void givesCheck_andTheOpponentMustAnswerIt() {
        place("king", Color.WHITE, "a1");
        place("cacique", Color.WHITE, "d4");
        place("king", Color.BLACK, "e8");
        place("pawn", Color.BLACK, "h7");
        Game game = start(FullGameWiring.twoPlayers(board, pieceFactory));

        assertTrue(game.move(move("d4", "d6")));

        assertEquals(GameStatus.CHECK, game.getStatus());
        assertTrue(spy.events.contains("check BLACK"));
        assertFalse(game.getLegalMoves().isEmpty());
        assertTrue(game.getLegalMoves().stream().allMatch(m -> m.getFrom().equals(at("e8"))),
                "only king moves get out of the cacique's check");
        assertFalse(game.move(move("h7", "h6")), "ignoring the check is illegal");
    }

    @Test
    void capture_thenUndo_restoresBothPieces() {
        place("king", Color.WHITE, "a1");
        place("king", Color.BLACK, "h8");
        place("cacique", Color.WHITE, "d4");
        place("rook", Color.BLACK, "d6");
        Game game = start(FullGameWiring.twoPlayers(board, pieceFactory));

        assertTrue(game.move(move("d4", "d6")));
        assertEquals("cacique", board.getPiece(at("d6")).getId());

        game.undo();

        assertEquals("cacique", board.getPiece(at("d4")).getId());
        assertEquals("rook", board.getPiece(at("d6")).getId());
        assertEquals(Color.WHITE, game.getCurrentTurn());
    }

    @Test
    void pawn_canPromoteToTheCacique() {
        place("king", Color.WHITE, "e1");
        place("king", Color.BLACK, "h6");
        place("pawn", Color.WHITE, "a7");
        Game game = start(FullGameWiring.twoPlayers(board, pieceFactory));

        assertTrue(game.move(new Move(at("a7"), at("a8"), "cacique")));

        Piece promoted = board.getPiece(at("a8"));
        assertEquals("cacique", promoted.getId());
        assertEquals(Color.WHITE, promoted.getColor());
    }

    @Test
    void computer_playsTheCaciqueWhenItWinsMaterial() {
        place("king", Color.WHITE, "a1");
        place("queen", Color.WHITE, "d6");
        place("king", Color.BLACK, "h8");
        place("cacique", Color.BLACK, "d4");
        Game game = start(FullGameWiring.versusComputer(board, pieceFactory));
        assertTrue(game.move(move("a1", "b1")));

        assertTrue(game.playTurn());

        Piece onD6 = board.getPiece(at("d6"));
        assertEquals("cacique", onD6.getId(), "the AI captures the undefended queen");
        assertEquals(Color.BLACK, onD6.getColor());
    }
}
