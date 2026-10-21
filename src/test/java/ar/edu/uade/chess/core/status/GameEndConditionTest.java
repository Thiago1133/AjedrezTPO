package ar.edu.uade.chess.core.status;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveCommandFactory;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.game.GameStatus;
import ar.edu.uade.chess.core.piece.KingDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.PieceDefinition;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.rules.CheckDetector;
import ar.edu.uade.chess.core.rules.MoveValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameEndConditionTest {

    private final Board board = new Board(8, 8);
    private final MoveHistory history = new MoveHistory();
    private final CheckDetector checkDetector = new CheckDetector();
    private final MoveCommandFactory commandFactory = new MoveCommandFactory(List.of());
    private final MoveValidator validator = new MoveValidator(checkDetector, commandFactory);
    private final CheckmateCondition checkmate = new CheckmateCondition(validator, checkDetector);
    private final StalemateCondition stalemate = new StalemateCondition(validator, checkDetector);
    private final FiftyMoveRuleCondition fiftyMoves = new FiftyMoveRuleCondition();
    private final ThreefoldRepetitionCondition repetition = new ThreefoldRepetitionCondition();

    private static Position at(String square) {
        return new Position(square.charAt(1) - '1', square.charAt(0) - 'a');
    }

    private void place(PieceDefinition definition, Color color, String square) {
        board.placePiece(definition.create(color), at(square));
    }

    /** Plays a move the same way Game does, asserting it is legal. */
    private void play(String from, String to, Color color) {
        Move move = new Move(at(from), at(to));
        assertTrue(validator.isLegal(board, move, history, color), "illegal move in test: " + move);
        MoveCommand command = commandFactory.create(board, move, history);
        command.execute(board);
        history.push(command);
    }

    /** Kings plus one knight each: lets the knights shuffle back and forth. */
    private void setUpKnightsOnly() {
        place(new KingDefinition(), Color.WHITE, "e1");
        place(new KingDefinition(), Color.BLACK, "e8");
        place(new KnightDefinition(), Color.WHITE, "g1");
        place(new KnightDefinition(), Color.BLACK, "g8");
    }

    /** One full cycle (4 half-moves) that returns to the same position. */
    private void shuffleKnights() {
        play("g1", "f3", Color.WHITE);
        play("g8", "f6", Color.BLACK);
        play("f3", "g1", Color.WHITE);
        play("f6", "g8", Color.BLACK);
    }

    @Test
    void backRankMate_isCheckmate() {
        place(new KingDefinition(), Color.WHITE, "e1");
        place(new RookDefinition(), Color.WHITE, "a1");
        place(new KingDefinition(), Color.BLACK, "h8");
        place(new PawnDefinition(), Color.BLACK, "g7");
        place(new PawnDefinition(), Color.BLACK, "h7");

        play("a1", "a8", Color.WHITE);

        assertTrue(checkmate.isMet(board, history, Color.BLACK));
        assertFalse(stalemate.isMet(board, history, Color.BLACK));
        assertEquals(GameStatus.CHECKMATE, checkmate.getResult());
    }

    @Test
    void checkWithAnEscape_isNotCheckmate() {
        place(new KingDefinition(), Color.WHITE, "e1");
        place(new RookDefinition(), Color.WHITE, "a1");
        place(new KingDefinition(), Color.BLACK, "h8");
        place(new PawnDefinition(), Color.BLACK, "g7");

        play("a1", "a8", Color.WHITE);

        assertTrue(checkDetector.isInCheck(board, Color.BLACK));
        assertFalse(checkmate.isMet(board, history, Color.BLACK), "king can go to h7");
    }

    @Test
    void kingWithNoMovesAndNotInCheck_isStalemate() {
        place(new KingDefinition(), Color.BLACK, "a8");
        place(new QueenDefinition(), Color.WHITE, "c7");
        place(new KingDefinition(), Color.WHITE, "c1");

        assertTrue(stalemate.isMet(board, history, Color.BLACK));
        assertFalse(checkmate.isMet(board, history, Color.BLACK));
        assertEquals(GameStatus.STALEMATE, stalemate.getResult());
    }

    @Test
    void fiftyMoveRule_triggersAfterOneHundredReversibleHalfMoves() {
        setUpKnightsOnly();
        for (int i = 0; i < 24; i++) {
            shuffleKnights();
        }
        play("g1", "f3", Color.WHITE);
        play("g8", "f6", Color.BLACK);
        play("f3", "g1", Color.WHITE);
        assertFalse(fiftyMoves.isMet(board, history, Color.BLACK), "99 half-moves");

        play("f6", "g8", Color.BLACK);

        assertTrue(fiftyMoves.isMet(board, history, Color.WHITE), "100 half-moves");
        assertEquals(GameStatus.DRAW, fiftyMoves.getResult());
    }

    @Test
    void fiftyMoveRule_isResetByPawnMovesAndCaptures() {
        setUpKnightsOnly();
        place(new PawnDefinition(), Color.WHITE, "a2");
        place(new PawnDefinition(), Color.BLACK, "h7");
        for (int i = 0; i < 24; i++) {
            shuffleKnights();
        }
        play("a2", "a3", Color.WHITE);
        play("g8", "f6", Color.BLACK);
        play("g1", "f3", Color.WHITE);
        play("f6", "g8", Color.BLACK);

        assertFalse(fiftyMoves.isMet(board, history, Color.WHITE));
    }

    @Test
    void threefoldRepetition_triggersOnThirdOccurrence() {
        setUpKnightsOnly();

        shuffleKnights();
        assertFalse(repetition.isMet(board, history, Color.WHITE), "second occurrence");

        shuffleKnights();
        assertTrue(repetition.isMet(board, history, Color.WHITE), "third occurrence");
        assertEquals(GameStatus.DRAW, repetition.getResult());
    }

    @Test
    void threefoldRepetition_ignoresPositionsBeforeAnIrreversibleMove() {
        setUpKnightsOnly();
        place(new PawnDefinition(), Color.WHITE, "a2");
        place(new PawnDefinition(), Color.BLACK, "a7");
        shuffleKnights();
        shuffleKnights();
        play("a2", "a3", Color.WHITE);
        play("a7", "a6", Color.BLACK);

        shuffleKnights();

        assertFalse(repetition.isMet(board, history, Color.WHITE), "only two occurrences since the pawn moves");
    }

    @Test
    void sameSquaresButDifferentPlayerToMove_isNotARepetition() {
        setUpKnightsOnly();
        play("g1", "f3", Color.WHITE);
        play("g8", "f6", Color.BLACK);
        play("f3", "g1", Color.WHITE);

        assertFalse(repetition.isMet(board, history, Color.BLACK));
    }
}
