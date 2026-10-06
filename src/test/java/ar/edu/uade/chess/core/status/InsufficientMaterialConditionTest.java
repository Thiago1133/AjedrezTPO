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
        assertDraw(position("bishop", Color.WHITE, "c1"));
        assertDraw(position("bishop", Color.BLACK, "c8"));
    }

    @Test
    void kingAndKnightVsKing_isDrawForEitherSide() {
        assertDraw(position("knight", Color.WHITE, "b1"));
        assertDraw(position("knight", Color.BLACK, "b8"));
    }

    @Test
    void oppositeBishopsOnSameSquareColor_isDraw() {
        assertDraw(position("bishop", Color.WHITE, "c1", "bishop", Color.BLACK, "f8"));
    }

    @Test
    void oppositeBishopsOnDifferentSquareColors_isNotInsufficientMaterialDraw() {
        assertNotDraw(position("bishop", Color.WHITE, "c1", "bishop", Color.BLACK, "g8"));
    }

    @Test
    void rookOrQueenVsKing_isNotInsufficientMaterialDraw() {
        assertNotDraw(position("rook", Color.WHITE, "a1"));
        assertNotDraw(position("queen", Color.WHITE, "a1"));
    }

    @Test
    void bishopAndKnightVsKing_isNotInsufficientMaterialDraw() {
        assertNotDraw(position("bishop", Color.WHITE, "a1", "knight", Color.WHITE, "b3"));
    }

    @Test
    void twoKnightsVsKing_isNotInsufficientMaterialDraw() {
        assertNotDraw(position("knight", Color.WHITE, "b1", "knight", Color.WHITE, "g1"));
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

    private Board position(Object... extraPieceDefinitions) {
        Board board = new Board(8, 8);
        place(board, new KingDefinition(), Color.WHITE, "e1");
        place(board, new KingDefinition(), Color.BLACK, "e8");
        for (int i = 0; i < extraPieceDefinitions.length; i += 3) {
            String id = (String) extraPieceDefinitions[i];
            Color color = (Color) extraPieceDefinitions[i + 1];
            String square = (String) extraPieceDefinitions[i + 2];
            place(board, definition(id), color, square);
        }
        return board;
    }

    private void place(Board board, PieceDefinition definition, Color color, String square) {
        Position position = new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
        board.placePiece(definition.create(color), position);
    }

    private PieceDefinition definition(String id) {
        return switch (id) {
            case "bishop" -> new BishopDefinition();
            case "knight" -> new KnightDefinition();
            case "rook" -> new RookDefinition();
            case "queen" -> new QueenDefinition();
            default -> throw new IllegalArgumentException("Unsupported test piece: " + id);
        };
    }
}
