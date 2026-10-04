package ar.edu.uade.chess.core.command;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.rules.SpecialMoveRule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class MoveCommandTest {

    private final Board board = new Board(8, 8);

    private static Position at(int row, int column) {
        return new Position(row, column);
    }

    /** Text snapshot of the board (piece, color and moved flag per square) to compare states. */
    private static Map<String, String> snapshot(Board board) {
        Map<String, String> result = new TreeMap<>();
        for (Color color : Color.values()) {
            for (Position position : board.getPositionsOf(color)) {
                Piece piece = board.getPiece(position);
                result.put(position.toString(), piece + (piece.hasMoved() ? " moved" : ""));
            }
        }
        return result;
    }

    @Test
    void execute_movesPieceAndMarksItAsMoved() {
        Piece rook = new RookDefinition().create(Color.WHITE);
        board.placePiece(rook, at(0, 0));

        new NormalMoveCommand(new Move(at(0, 0), at(5, 0))).execute(board);

        assertTrue(board.isEmpty(at(0, 0)));
        assertSame(rook, board.getPiece(at(5, 0)));
        assertTrue(rook.hasMoved());
    }

    @Test
    void execute_thenUndo_restoresOriginalBoard() {
        board.placePiece(new RookDefinition().create(Color.WHITE), at(0, 0));
        board.placePiece(new PawnDefinition().create(Color.BLACK), at(0, 6));
        Map<String, String> before = snapshot(board);
        MoveCommand command = new NormalMoveCommand(new Move(at(0, 0), at(0, 6)));

        command.execute(board);
        assertNotEquals(before, snapshot(board));
        command.undo(board);

        assertEquals(before, snapshot(board));
    }

    @Test
    void undo_restoresCapturedPieceInstance() {
        Piece victim = new PawnDefinition().create(Color.BLACK);
        board.placePiece(new KnightDefinition().create(Color.WHITE), at(0, 1));
        board.placePiece(victim, at(2, 2));
        MoveCommand command = new NormalMoveCommand(new Move(at(0, 1), at(2, 2)));

        command.execute(board);
        command.undo(board);

        assertSame(victim, board.getPiece(at(2, 2)));
    }

    @Test
    void undo_keepsMovedFlagIfPieceHadAlreadyMoved() {
        Piece rook = new RookDefinition().create(Color.WHITE);
        rook.setMoved(true);
        board.placePiece(rook, at(0, 0));
        MoveCommand command = new NormalMoveCommand(new Move(at(0, 0), at(0, 3)));

        command.execute(board);
        command.undo(board);

        assertTrue(rook.hasMoved());
    }

    @Test
    void isIrreversible_onlyForCapturesAndPawnMoves() {
        board.placePiece(new RookDefinition().create(Color.WHITE), at(0, 0));
        board.placePiece(new PawnDefinition().create(Color.WHITE), at(1, 7));
        board.placePiece(new KnightDefinition().create(Color.BLACK), at(5, 0));

        MoveCommand rookMove = new NormalMoveCommand(new Move(at(0, 0), at(3, 0)));
        rookMove.execute(board);
        MoveCommand pawnMove = new NormalMoveCommand(new Move(at(1, 7), at(2, 7)));
        pawnMove.execute(board);
        MoveCommand capture = new NormalMoveCommand(new Move(at(3, 0), at(5, 0)));
        capture.execute(board);

        assertFalse(rookMove.isIrreversible());
        assertTrue(pawnMove.isIrreversible());
        assertTrue(capture.isIrreversible());
    }

    @Test
    void execute_withoutPiece_throws() {
        MoveCommand command = new NormalMoveCommand(new Move(at(0, 0), at(1, 0)));
        assertThrows(IllegalStateException.class, () -> command.execute(board));
    }

    @Test
    void history_undoAndRedoFollowStackOrder() {
        MoveHistory history = new MoveHistory();
        MoveCommand first = new NormalMoveCommand(new Move(at(1, 0), at(2, 0)));
        MoveCommand second = new NormalMoveCommand(new Move(at(6, 0), at(5, 0)));
        history.push(first);
        history.push(second);

        assertEquals(second.getMove(), history.getLastMove());
        assertSame(second, history.popForUndo());
        assertEquals(first.getMove(), history.getLastMove());
        assertSame(second, history.popForRedo());
        assertEquals(List.of(first, second), history.getExecuted());
    }

    @Test
    void history_newMoveClearsRedo() {
        MoveHistory history = new MoveHistory();
        history.push(new NormalMoveCommand(new Move(at(1, 0), at(2, 0))));
        history.popForUndo();

        history.push(new NormalMoveCommand(new Move(at(1, 1), at(2, 1))));

        assertNull(history.popForRedo());
    }

    @Test
    void history_emptyReturnsNull() {
        MoveHistory history = new MoveHistory();
        assertNull(history.popForUndo());
        assertNull(history.popForRedo());
        assertNull(history.getLastMove());
    }

    @Test
    void factory_withoutSpecialRules_createsNormalCommand() {
        MoveCommandFactory factory = new MoveCommandFactory(List.of());
        Move move = new Move(at(0, 0), at(1, 0));

        assertFalse(factory.isSpecialMove(board, move, new MoveHistory()));
        assertInstanceOf(NormalMoveCommand.class, factory.create(board, move, new MoveHistory()));
    }

    @Test
    void factory_delegatesToApplicableSpecialRule() {
        Move special = new Move(at(0, 0), at(7, 7));
        MoveCommand specialCommand = new NormalMoveCommand(special);
        SpecialMoveRule stubRule = new SpecialMoveRule() {
            @Override
            public boolean canApply(Board board, Move move, MoveHistory history) {
                return move.equals(special);
            }

            @Override
            public MoveCommand createCommand(Board board, Move move) {
                return specialCommand;
            }

            @Override
            public List<Move> getCandidateMoves(Board board, Position from, MoveHistory history) {
                return List.of(special);
            }
        };
        MoveCommandFactory factory = new MoveCommandFactory(List.of(stubRule));

        assertTrue(factory.isSpecialMove(board, special, new MoveHistory()));
        assertSame(specialCommand, factory.create(board, special, new MoveHistory()));
        assertEquals(List.of(special), factory.getSpecialMoves(board, at(0, 0), new MoveHistory()));
        assertInstanceOf(NormalMoveCommand.class,
                factory.create(board, new Move(at(0, 0), at(1, 0)), new MoveHistory()));
    }
}
