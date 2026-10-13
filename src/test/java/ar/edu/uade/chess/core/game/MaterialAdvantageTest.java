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
    void equalMaterial_andKingsAlone_doNotCreateAnAdvantage() {
        Game kingsOnly = start();
        assertEquals(0, kingsOnly.getMaterialAdvantage(Color.WHITE));
        assertEquals(0, kingsOnly.getMaterialAdvantage(Color.BLACK));

        board = new Board(8, 8);
        place("pawn", Color.WHITE, "a2");
        place("pawn", Color.BLACK, "h7");
        Game equalPawns = start();
        assertEquals(0, equalPawns.getMaterialAdvantage(Color.WHITE));
        assertEquals(0, equalPawns.getMaterialAdvantage(Color.BLACK));
    }

    @Test
    void capturingAPawn_givesOnePointOfCurrentMaterialAdvantage() {
        place("rook", Color.WHITE, "a1");
        place("pawn", Color.BLACK, "a7");
        place("pawn", Color.WHITE, "h2");
        place("rook", Color.BLACK, "h8");
        Game game = startWithKingsAt("e1", "e8");

        assertTrue(game.move(move("a1", "a7")));

        assertEquals(1, game.getMaterialAdvantage(Color.WHITE));
        assertEquals(0, game.getMaterialAdvantage(Color.BLACK));
        assertEquals(List.of("pawn"), game.getCapturedPieces(Color.WHITE).stream().map(Piece::getId).toList());
    }

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
    void promotingToAQueen_addsEightPoints() {
        balancedPromotionPawns();
        Game game = start();

        assertTrue(game.move(promotion("a7", "a8", "queen")));

        assertEquals(8, game.getMaterialAdvantage(Color.WHITE));
    }

    @Test
    void blackPromotionToAKnight_addsTwoPointsForBlack() {
        place("pawn", Color.WHITE, "h7");
        place("pawn", Color.BLACK, "a2");
        Game game = start();

        assertTrue(game.move(promotion("a2", "a1", "knight")));

        assertEquals(2, game.getMaterialAdvantage(Color.BLACK));
        assertEquals(0, game.getMaterialAdvantage(Color.WHITE));
    }

    @Test
    void blackPromotionToARook_reversesAThreePointWhiteLeadToOneForBlack() {
        place("knight", Color.WHITE, "b1");
        place("pawn", Color.WHITE, "h7");
        place("pawn", Color.BLACK, "a2");
        Game game = start();
        assertEquals(3, game.getMaterialAdvantage(Color.WHITE));

        assertTrue(game.move(promotion("a2", "a1", "rook")));

        assertEquals(1, game.getMaterialAdvantage(Color.BLACK));
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

    @Test
    void promotingToABishop_addsTwoPoints() {
        balancedPromotionPawns();
        Game game = start();
        assertTrue(game.move(promotion("a7", "a8", "bishop")));
        assertEquals(2, game.getMaterialAdvantage(Color.WHITE));
    }

    @Test
    void promotingToAKnight_addsTwoPoints() {
        balancedPromotionPawns();
        Game game = start();
        assertTrue(game.move(promotion("a7", "a8", "knight")));
        assertEquals(2, game.getMaterialAdvantage(Color.WHITE));
    }

    @Test
    void queenPromotionTurnsAnExistingTwoPointLeadIntoTen() {
        place("rook", Color.WHITE, "c1");
        place("bishop", Color.BLACK, "f8");
        place("pawn", Color.WHITE, "a7");
        place("pawn", Color.BLACK, "h2");
        Game game = start();
        assertEquals(2, game.getMaterialAdvantage(Color.WHITE));

        assertTrue(game.move(promotion("a7", "a8", "queen")));

        assertEquals(10, game.getMaterialAdvantage(Color.WHITE));
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
