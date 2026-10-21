package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaterialAdvantageTest {
    private final PieceFactory pieceFactory = FullGameWiring.standardPieces();
    private Board board = new Board(8, 8);

    @Test
    void promotingToARook_addsFourPoints_andUndoRedoRecalculateFromTheBoard() {
        balancedPromotionPawns();
        Game game = start();

        assertTrue(game.move(promotion("a7", "a8", "rook")));
        assertEquals(4, game.getMaterialAdvantage(Color.WHITE));
        assertTrue(game.getCapturedPieces(Color.WHITE).isEmpty());
        assertTrue(game.getCapturedPieces(Color.BLACK).isEmpty());

        game.undo();
        assertEquals(0, game.getMaterialAdvantage(Color.WHITE));
        game.redo();
        assertEquals(4, game.getMaterialAdvantage(Color.WHITE));
    }

    @Test
    void blackPromotionToAKnight_addsTwoPointsForBlack() {
        place("pawn", Color.WHITE, "h7");
        place("pawn", Color.BLACK, "a2");
        Game game = start();
        assertTrue(game.move(move("e1", "e2")), "white moves first");

        assertTrue(game.move(promotion("a2", "a1", "knight")));

        assertEquals(2, game.getMaterialAdvantage(Color.BLACK));
        assertEquals(0, game.getMaterialAdvantage(Color.WHITE));
    }

    @Test
    void promotingWithACapture_countsTheMaterialUpgradeAndCapturedPiece_butListsOnlyCapture() {
        place("pawn", Color.WHITE, "b7");
        place("pawn", Color.WHITE, "h2");
        place("rook", Color.BLACK, "a8");
        place("pawn", Color.BLACK, "h7");
        Game game = start();
        assertEquals(4, game.getMaterialAdvantage(Color.BLACK));

        assertTrue(game.move(promotion("b7", "a8", "queen")));

        assertEquals(9, game.getMaterialAdvantage(Color.WHITE));
        assertEquals(List.of("rook"), game.getCapturedPieces(Color.WHITE).stream().map(Piece::getId).toList());
        assertTrue(game.getCapturedPieces(Color.BLACK).isEmpty());
    }

    private void balancedPromotionPawns() {
        place("pawn", Color.WHITE, "a7");
        place("pawn", Color.BLACK, "h2");
    }

    private Game start() {
        return startWithKingsAt("e1", "h8");
    }

    private Game startWithKingsAt(String whiteKing, String blackKing) {
        place("king", Color.WHITE, whiteKing);
        place("king", Color.BLACK, blackKing);
        Game game = FullGameWiring.twoPlayers(board, pieceFactory);
        game.start();
        return game;
    }

    private void place(String id, Color color, String square) {
        board.placePiece(pieceFactory.create(id, color), position(square));
    }

    private static Move move(String from, String to) {
        return new Move(position(from), position(to));
    }

    private static Move promotion(String from, String to, String pieceId) {
        return new Move(position(from), position(to), pieceId);
    }

    private static Position position(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }
}
