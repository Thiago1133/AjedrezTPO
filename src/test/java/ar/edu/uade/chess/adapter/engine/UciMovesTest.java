package ar.edu.uade.chess.adapter.engine;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.BishopDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Reading an engine's UCI output, from fixed text: no engine is started. */
class UciMovesTest {
    private static final List<Piece> PROMOTIONS = List.of(
            new QueenDefinition().create(Color.WHITE), new RookDefinition().create(Color.WHITE),
            new KnightDefinition().create(Color.WHITE), new BishopDefinition().create(Color.WHITE));

    private static Move move(String from, String to) {
        return new Move(new Position(from.charAt(1) - '1', from.charAt(0) - 'a'),
                new Position(to.charAt(1) - '1', to.charAt(0) - 'a'));
    }

    @Test
    void candidates_areOrderedByRank_andTakenFromTheDeepestSearch() {
        List<String> output = List.of(
                "info string NNUE evaluation enabled",
                "info depth 1 seldepth 1 multipv 1 score cp 30 nodes 20 pv d2d4",
                "info depth 1 seldepth 1 multipv 2 score cp 20 nodes 40 pv g1f3",
                "info depth 2 seldepth 2 multipv 2 score cp 25 nodes 90 pv e2e4 e7e5",
                "info depth 2 seldepth 2 multipv 1 score cp 35 nodes 99 pv c2c4 e7e5",
                "bestmove c2c4 ponder e7e5");

        assertEquals(List.of(move("c2", "c4"), move("e2", "e4")), UciMoves.candidates(output, PROMOTIONS));
    }

    @Test
    void withoutCandidateLines_theBestMoveAlone() {
        List<String> output = List.of("info depth 5 score cp 10 pv e2e4", "bestmove e2e4");

        assertEquals(List.of(move("e2", "e4")), UciMoves.candidates(output, PROMOTIONS));
    }

    @Test
    void aPositionWithoutMoves_givesNoCandidates() {
        assertEquals(List.of(), UciMoves.candidates(List.of("bestmove (none)"), PROMOTIONS));
    }

    @Test
    void promotion_isReadWithThePieceItBecomes() {
        Move promotion = UciMoves.parse("a7a8n", PROMOTIONS).orElseThrow();

        assertEquals(move("a7", "a8"), promotion);
        assertEquals("knight", promotion.getPromotionId());
    }

    @Test
    void textThatIsNotAMove_isIgnored() {
        assertEquals(Optional.empty(), UciMoves.parse("e2", PROMOTIONS));
        assertEquals(Optional.empty(), UciMoves.parse("z9z9", PROMOTIONS));
    }
}
