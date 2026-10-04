package ar.edu.uade.chess.core.rules;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveCommandFactory;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.command.NormalMoveCommand;
import ar.edu.uade.chess.core.piece.BishopDefinition;
import ar.edu.uade.chess.core.piece.KingDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.PieceDefinition;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.piece.StandardChessSetup;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveValidatorTest {

    private final Board board = new Board(8, 8);
    private final MoveHistory history = new MoveHistory();
    private final MoveValidator validator =
            new MoveValidator(new CheckDetector(), new MoveCommandFactory(List.of()));

    private void place(PieceDefinition definition, Color color, int row, int column) {
        board.placePiece(definition.create(color), new Position(row, column));
    }

    private boolean legal(int fromRow, int fromColumn, int toRow, int toColumn, Color color) {
        Move move = new Move(new Position(fromRow, fromColumn), new Position(toRow, toColumn));
        return validator.isLegal(board, move, history, color);
    }

    @Test
    void validMove_isLegal() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new RookDefinition(), Color.WHITE, 0, 0);

        assertTrue(legal(0, 0, 5, 0, Color.WHITE));
    }

    @Test
    void moveForbiddenByPieceRules_isIllegal() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new RookDefinition(), Color.WHITE, 0, 0);

        assertFalse(legal(0, 0, 3, 3, Color.WHITE));
    }

    @Test
    void cannotMoveOpponentPiece_orFromEmptySquare() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new RookDefinition(), Color.BLACK, 7, 0);

        assertFalse(legal(7, 0, 6, 0, Color.WHITE), "not your piece");
        assertFalse(legal(3, 3, 4, 3, Color.WHITE), "empty square");
    }

    @Test
    void movesOutsideBoardOrToSameSquare_areIllegal() {
        place(new KingDefinition(), Color.WHITE, 0, 4);

        assertFalse(legal(0, 4, -1, 4, Color.WHITE));
        assertFalse(legal(0, 4, 0, 4, Color.WHITE));
    }

    @Test
    void captureOfEnemyPiece_isLegal_ownPieceIsNot() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new RookDefinition(), Color.WHITE, 0, 0);
        place(new KnightDefinition(), Color.BLACK, 4, 0);
        place(new PawnDefinition(), Color.WHITE, 0, 2);

        assertTrue(legal(0, 0, 4, 0, Color.WHITE));
        assertFalse(legal(0, 0, 0, 2, Color.WHITE));
    }

    @Test
    void pinnedPiece_cannotExposeKing() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new BishopDefinition(), Color.WHITE, 1, 4);
        place(new RookDefinition(), Color.BLACK, 7, 4);

        assertFalse(legal(1, 4, 2, 5, Color.WHITE));
    }

    @Test
    void king_cannotMoveIntoCheck() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new RookDefinition(), Color.BLACK, 7, 5);

        assertFalse(legal(0, 4, 0, 5, Color.WHITE));
        assertTrue(legal(0, 4, 0, 3, Color.WHITE));
    }

    @Test
    void whenInCheck_onlyMovesThatResolveItAreLegal() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new RookDefinition(), Color.WHITE, 0, 0);
        place(new KnightDefinition(), Color.WHITE, 2, 1);
        place(new QueenDefinition(), Color.BLACK, 4, 4);

        assertFalse(legal(0, 0, 0, 1, Color.WHITE), "rook move does not address the check");
        assertFalse(legal(2, 1, 4, 0, Color.WHITE), "knight move does not block");
        assertFalse(legal(0, 4, 1, 4, Color.WHITE), "king stays on the attacked file");
        assertTrue(legal(0, 4, 0, 3, Color.WHITE), "king steps aside");
    }

    @Test
    void blockingOrCapturingTheChecker_isLegal() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new RookDefinition(), Color.WHITE, 2, 0);
        place(new KnightDefinition(), Color.WHITE, 2, 5);
        place(new QueenDefinition(), Color.BLACK, 4, 4);

        assertTrue(legal(2, 0, 2, 4, Color.WHITE), "rook blocks");
        assertTrue(legal(2, 5, 4, 4, Color.WHITE), "knight captures the queen");
    }

    @Test
    void validation_doesNotModifyBoard() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        place(new RookDefinition(), Color.WHITE, 0, 0);

        legal(0, 0, 5, 0, Color.WHITE);

        assertNotNull(board.getPiece(new Position(0, 0)));
        assertTrue(board.isEmpty(new Position(5, 0)));
        assertFalse(board.getPiece(new Position(0, 0)).hasMoved());
    }

    @Test
    void initialPosition_hasTwentyLegalMoves() {
        PieceFactory factory = new PieceFactory(List.of(
                new PawnDefinition(), new RookDefinition(), new KnightDefinition(),
                new BishopDefinition(), new QueenDefinition(), new KingDefinition()));
        new StandardChessSetup(factory).setup(board);

        assertEquals(20, validator.getLegalMoves(board, history, Color.WHITE).size());
        assertEquals(20, validator.getLegalMoves(board, history, Color.BLACK).size());
    }

    @Test
    void injectedSpecialRule_allowsMoveThatPiecesCannotMake() {
        place(new KingDefinition(), Color.WHITE, 0, 4);
        Move teleport = new Move(new Position(0, 4), new Position(3, 0));
        SpecialMoveRule teleportRule = new SpecialMoveRule() {
            @Override
            public boolean canApply(Board board, Move move, MoveHistory history) {
                return move.equals(teleport);
            }

            @Override
            public MoveCommand createCommand(Board board, Move move) {
                return new NormalMoveCommand(move);
            }

            @Override
            public List<Move> getCandidateMoves(Board board, Position from, MoveHistory history) {
                return from.equals(teleport.getFrom()) ? List.of(teleport) : List.of();
            }
        };
        MoveValidator withRule = new MoveValidator(new CheckDetector(), new MoveCommandFactory(List.of(teleportRule)));

        assertTrue(withRule.isLegal(board, teleport, history, Color.WHITE));
        assertTrue(withRule.getLegalMoves(board, history, Color.WHITE).contains(teleport));
        assertFalse(validator.isLegal(board, teleport, history, Color.WHITE));
    }
}
