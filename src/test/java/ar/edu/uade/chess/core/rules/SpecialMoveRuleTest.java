package ar.edu.uade.chess.core.rules;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.CastlingCommand;
import ar.edu.uade.chess.core.command.EnPassantCommand;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveCommandFactory;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.command.PromotionCommand;
import ar.edu.uade.chess.core.piece.BishopDefinition;
import ar.edu.uade.chess.core.piece.KingDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceDefinition;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class SpecialMoveRuleTest {

    private final Board board = new Board(8, 8);
    private final MoveHistory history = new MoveHistory();
    private final PieceFactory pieceFactory = new PieceFactory(List.of(
            new PawnDefinition(), new RookDefinition(), new KnightDefinition(),
            new BishopDefinition(), new QueenDefinition(), new KingDefinition()));
    private final CheckDetector checkDetector = new CheckDetector();
    private final MoveCommandFactory commandFactory = new MoveCommandFactory(List.of(
            new CastlingRule(checkDetector), new EnPassantRule(), new PromotionRule(pieceFactory)));
    private final MoveValidator validator = new MoveValidator(checkDetector, commandFactory);

    private static Position at(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    private static Move move(String from, String to) {
        return new Move(at(from), at(to));
    }

    private Piece place(PieceDefinition definition, Color color, String square) {
        Piece piece = definition.create(color);
        board.placePiece(piece, at(square));
        return piece;
    }

    private boolean legal(Move move, Color color) {
        return validator.isLegal(board, move, history, color);
    }

    private MoveCommand play(Move move, Color color) {
        assertTrue(legal(move, color), "illegal move in test: " + move);
        MoveCommand command = commandFactory.create(board, move, history);
        command.execute(board);
        history.push(command);
        return command;
    }

    private Map<String, String> snapshot() {
        Map<String, String> result = new TreeMap<>();
        for (Color color : Color.values()) {
            for (Position position : board.getPositionsOf(color)) {
                Piece piece = board.getPiece(position);
                result.put(position.toString(), piece + (piece.hasMoved() ? " moved" : ""));
            }
        }
        return result;
    }

    @Nested
    class Castling {

        private void setUpCastlingPosition() {
            place(new KingDefinition(), Color.WHITE, "e1");
            place(new RookDefinition(), Color.WHITE, "a1");
            place(new RookDefinition(), Color.WHITE, "h1");
            place(new KingDefinition(), Color.BLACK, "e8");
        }

        @Test
        void kingside_movesKingAndRook() {
            setUpCastlingPosition();

            MoveCommand command = play(move("e1", "g1"), Color.WHITE);

            assertInstanceOf(CastlingCommand.class, command);
            assertEquals("king", board.getPiece(at("g1")).getId());
            assertEquals("rook", board.getPiece(at("f1")).getId());
            assertTrue(board.isEmpty(at("e1")));
            assertTrue(board.isEmpty(at("h1")));
        }

        @Test
        void queenside_movesKingAndRook() {
            setUpCastlingPosition();

            play(move("e1", "c1"), Color.WHITE);

            assertEquals("king", board.getPiece(at("c1")).getId());
            assertEquals("rook", board.getPiece(at("d1")).getId());
            assertTrue(board.isEmpty(at("a1")));
        }

        @Test
        void undo_restoresKingRookAndCastlingRights() {
            setUpCastlingPosition();
            Map<String, String> before = snapshot();

            MoveCommand command = play(move("e1", "g1"), Color.WHITE);
            command.undo(board);

            assertEquals(before, snapshot());
            assertTrue(legal(move("e1", "g1"), Color.WHITE));
        }

        @Test
        void notAllowedIfKingHasMoved() {
            setUpCastlingPosition();
            board.getPiece(at("e1")).setMoved(true);

            assertFalse(legal(move("e1", "g1"), Color.WHITE));
        }

        @Test
        void notAllowedIfThatRookHasMoved() {
            setUpCastlingPosition();
            board.getPiece(at("h1")).setMoved(true);

            assertFalse(legal(move("e1", "g1"), Color.WHITE));
            assertTrue(legal(move("e1", "c1"), Color.WHITE), "the other side is still allowed");
        }

        @Test
        void notAllowedWithPiecesInBetween() {
            setUpCastlingPosition();
            place(new KnightDefinition(), Color.WHITE, "b1");

            assertFalse(legal(move("e1", "c1"), Color.WHITE));
        }

        @Test
        void notAllowedWhileInCheck() {
            setUpCastlingPosition();
            place(new RookDefinition(), Color.BLACK, "e5");

            assertFalse(legal(move("e1", "g1"), Color.WHITE));
        }

        @Test
        void notAllowedThroughAnAttackedSquare() {
            setUpCastlingPosition();
            place(new RookDefinition(), Color.BLACK, "f5");

            assertFalse(legal(move("e1", "g1"), Color.WHITE), "f1 is attacked");
        }

        @Test
        void notAllowedThroughASquareAttackedOnlyByAPawn() {
            setUpCastlingPosition();
            place(new PawnDefinition(), Color.BLACK, "e2");

            assertFalse(legal(move("e1", "g1"), Color.WHITE), "the pawn on e2 attacks f1");
        }

        @Test
        void notAllowedIntoCheck() {
            setUpCastlingPosition();
            place(new RookDefinition(), Color.BLACK, "g5");

            assertFalse(legal(move("e1", "g1"), Color.WHITE));
        }

        @Test
        void isListedAmongLegalMoves() {
            setUpCastlingPosition();

            List<Move> legalMoves = validator.getLegalMoves(board, history, Color.WHITE);

            assertTrue(legalMoves.contains(move("e1", "g1")));
            assertTrue(legalMoves.contains(move("e1", "c1")));
        }
    }

    @Nested
    class EnPassant {

        private void setUpAfterBlackDoubleStep() {
            place(new KingDefinition(), Color.WHITE, "e1");
            place(new KingDefinition(), Color.BLACK, "e8");
            place(new PawnDefinition(), Color.WHITE, "e5").setMoved(true);
            place(new PawnDefinition(), Color.BLACK, "d7");
            play(move("d7", "d5"), Color.BLACK);
        }

        @Test
        void capturesThePawnThatJustDoubleStepped() {
            setUpAfterBlackDoubleStep();

            MoveCommand command = play(move("e5", "d6"), Color.WHITE);

            assertInstanceOf(EnPassantCommand.class, command);
            assertEquals(Color.WHITE, board.getPiece(at("d6")).getColor());
            assertTrue(board.isEmpty(at("d5")), "captured pawn removed");
            assertTrue(command.isIrreversible());
        }

        @Test
        void undo_putsTheCapturedPawnBack() {
            setUpAfterBlackDoubleStep();
            Map<String, String> before = snapshot();

            MoveCommand command = play(move("e5", "d6"), Color.WHITE);
            command.undo(board);

            assertEquals(before, snapshot());
        }

        @Test
        void onlyAvailableImmediately() {
            setUpAfterBlackDoubleStep();
            play(move("e1", "f1"), Color.WHITE);
            play(move("e8", "f8"), Color.BLACK);

            assertFalse(legal(move("e5", "d6"), Color.WHITE));
        }

        @Test
        void notAvailableAfterASingleStep() {
            place(new KingDefinition(), Color.WHITE, "e1");
            place(new KingDefinition(), Color.BLACK, "e8");
            place(new PawnDefinition(), Color.WHITE, "e5").setMoved(true);
            place(new PawnDefinition(), Color.BLACK, "d6").setMoved(true);
            play(move("d6", "d5"), Color.BLACK);

            assertFalse(legal(move("e5", "d6"), Color.WHITE));
        }

        @Test
        void isListedAmongLegalMoves() {
            setUpAfterBlackDoubleStep();

            assertTrue(validator.getLegalMoves(board, history, Color.WHITE).contains(move("e5", "d6")));
        }
    }

    @Nested
    class Promotion {

        private void setUpPawnAboutToPromote() {
            place(new KingDefinition(), Color.WHITE, "e1");
            place(new KingDefinition(), Color.BLACK, "h8");
            place(new PawnDefinition(), Color.WHITE, "a7").setMoved(true);
        }

        @Test
        void withoutChoice_promotesToQueen() {
            setUpPawnAboutToPromote();

            MoveCommand command = play(move("a7", "a8"), Color.WHITE);

            assertInstanceOf(PromotionCommand.class, command);
            assertEquals("queen", board.getPiece(at("a8")).getId());
            assertEquals(Color.WHITE, board.getPiece(at("a8")).getColor());
        }

        @Test
        void promotesToTheChosenPiece() {
            setUpPawnAboutToPromote();

            play(new Move(at("a7"), at("a8"), "knight"), Color.WHITE);

            assertEquals("knight", board.getPiece(at("a8")).getId());
        }

        @Test
        void invalidChoice_becomesQueen() {
            setUpPawnAboutToPromote();

            play(new Move(at("a7"), at("a8"), "king"), Color.WHITE);

            assertEquals("queen", board.getPiece(at("a8")).getId());
        }

        @Test
        void unknownChoice_becomesQueen() {
            setUpPawnAboutToPromote();

            play(new Move(at("a7"), at("a8"), "dragon"), Color.WHITE);

            assertEquals("queen", board.getPiece(at("a8")).getId());
        }

        @Test
        void captureWithPromotion_andUndoRestoresEverything() {
            setUpPawnAboutToPromote();
            place(new RookDefinition(), Color.BLACK, "b8");
            Map<String, String> before = snapshot();

            MoveCommand command = play(new Move(at("a7"), at("b8"), "rook"), Color.WHITE);
            assertEquals(Color.WHITE, board.getPiece(at("b8")).getColor());
            assertEquals("rook", board.getPiece(at("b8")).getId());
            command.undo(board);

            assertEquals(before, snapshot());
        }

        @Test
        void blackPromotesOnRowOne() {
            place(new KingDefinition(), Color.WHITE, "h1");
            place(new KingDefinition(), Color.BLACK, "e8");
            place(new PawnDefinition(), Color.BLACK, "c2").setMoved(true);

            play(move("c2", "c1"), Color.BLACK);

            assertEquals("queen", board.getPiece(at("c1")).getId());
            assertEquals(Color.BLACK, board.getPiece(at("c1")).getColor());
        }

        @Test
        void promotedQueen_givesCheck() {
            setUpPawnAboutToPromote();

            play(move("a7", "a8"), Color.WHITE);

            assertTrue(checkDetector.isInCheck(board, Color.BLACK), "queen on a8 attacks h8 along the row");
        }

        @Test
        void worksOnTallerBoards() {
            Board tall = new Board(12, 8);
            tall.placePiece(new KingDefinition().create(Color.WHITE), new Position(0, 4));
            tall.placePiece(new KingDefinition().create(Color.BLACK), new Position(11, 7));
            Piece pawn = new PawnDefinition().create(Color.WHITE);
            pawn.setMoved(true);
            tall.placePiece(pawn, new Position(10, 0));
            Move promotion = new Move(new Position(10, 0), new Position(11, 0));

            Piece otherPawn = new PawnDefinition().create(Color.WHITE);
            otherPawn.setMoved(true);
            tall.placePiece(otherPawn, new Position(6, 3));
            Move toRowEight = new Move(new Position(6, 3), new Position(7, 3));

            assertTrue(commandFactory.isSpecialMove(tall, promotion, history), "row 12 is the last row");
            assertFalse(commandFactory.isSpecialMove(tall, toRowEight, history), "row 8 is not the last row here");
        }
    }
}
