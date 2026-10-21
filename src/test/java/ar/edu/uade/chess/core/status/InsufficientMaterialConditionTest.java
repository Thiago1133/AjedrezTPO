package ar.edu.uade.chess.core.status;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.MoveCommandFactory;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.game.GameStatus;
import ar.edu.uade.chess.core.piece.BishopDefinition;
import ar.edu.uade.chess.core.piece.KingDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PieceDefinition;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.rules.CheckDetector;
import ar.edu.uade.chess.core.rules.MoveValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class InsufficientMaterialConditionTest {
    private static final PieceDefinition BISHOP = new BishopDefinition();
    private static final PieceDefinition KNIGHT = new KnightDefinition();
    private static final PieceDefinition ROOK = new RookDefinition();
    private static final PieceDefinition QUEEN = new QueenDefinition();

    private final CheckDetector checkDetector = new CheckDetector();
    private final MoveValidator moveValidator = new MoveValidator(checkDetector, new MoveCommandFactory(List.of()));
    private final GameStatusEvaluator evaluator = new GameStatusEvaluator(checkDetector, List.of(
            new CheckmateCondition(moveValidator, checkDetector),
            new StalemateCondition(moveValidator, checkDetector),
            new InsufficientMaterialCondition()));

    @Test
    void kingVsKing_isDraw() {
        assertDraw(position());
    }

    @Test
    void kingAndBishopVsKing_isDrawForEitherSide() {
        assertDraw(position(BISHOP, Color.WHITE, "c1"));
        assertDraw(position(BISHOP, Color.BLACK, "c8"));
    }

    @Test
    void kingAndKnightVsKing_isDrawForEitherSide() {
        assertDraw(position(KNIGHT, Color.WHITE, "b1"));
        assertDraw(position(KNIGHT, Color.BLACK, "b8"));
    }

    @Test
    void oppositeBishopsOnSameSquareColor_isDraw() {
        assertDraw(position(BISHOP, Color.WHITE, "c1", BISHOP, Color.BLACK, "f8"));
    }

    @Test
    void oppositeBishopsOnDifferentSquareColors_isNotInsufficientMaterialDraw() {
        assertNotDraw(position(BISHOP, Color.WHITE, "c1", BISHOP, Color.BLACK, "g8"));
    }

    @Test
    void rookOrQueenVsKing_isNotInsufficientMaterialDraw() {
        assertNotDraw(position(ROOK, Color.WHITE, "a1"));
        assertNotDraw(position(QUEEN, Color.WHITE, "a1"));
    }

    @Test
    void twoKnightsVsKing_isNotInsufficientMaterialDraw() {
        assertNotDraw(position(KNIGHT, Color.WHITE, "b1", KNIGHT, Color.WHITE, "g1"));
    }

    private void assertDraw(Board board) {
        assertEquals(GameStatus.DRAW, evaluate(board));
    }

    private void assertNotDraw(Board board) {
        assertNotEquals(GameStatus.DRAW, evaluate(board));
    }

    private GameStatus evaluate(Board board) {
        return evaluator.evaluate(board, new MoveHistory(), Color.WHITE);
    }

    /** Kings on e1 and e8 plus extra pieces given as (definition, color, square) triples. */
    private Board position(Object... extraPieces) {
        Board board = new Board(8, 8);
        place(board, new KingDefinition(), Color.WHITE, "e1");
        place(board, new KingDefinition(), Color.BLACK, "e8");
        for (int i = 0; i < extraPieces.length; i += 3) {
            place(board, (PieceDefinition) extraPieces[i], (Color) extraPieces[i + 1], (String) extraPieces[i + 2]);
        }
        return board;
    }

    private void place(Board board, PieceDefinition definition, Color color, String square) {
        board.placePiece(definition.create(color), at(square));
    }

    private static Position at(String square) {
        return new Position(Integer.parseInt(square.substring(1)) - 1, square.charAt(0) - 'a');
    }

}
