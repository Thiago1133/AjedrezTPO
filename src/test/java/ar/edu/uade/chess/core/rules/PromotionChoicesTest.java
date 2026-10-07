package ar.edu.uade.chess.core.rules;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Direction;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.piece.BishopDefinition;
import ar.edu.uade.chess.core.piece.KingDefinition;
import ar.edu.uade.chess.core.piece.KnightDefinition;
import ar.edu.uade.chess.core.piece.PawnDefinition;
import ar.edu.uade.chess.core.piece.Piece;
import ar.edu.uade.chess.core.piece.PieceDefinition;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.QueenDefinition;
import ar.edu.uade.chess.core.piece.RookDefinition;
import ar.edu.uade.chess.core.piece.SlidingMovement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** The promotion choices come from the registered pieces, so new pieces need no code changes. */
class PromotionChoicesTest {

    private final PieceFactory pieceFactory = new PieceFactory(List.of(
            new PawnDefinition(), new RookDefinition(), new KnightDefinition(),
            new BishopDefinition(), new QueenDefinition(), new KingDefinition()));
    private final PromotionRule rule = new PromotionRule(pieceFactory);

    private static final PieceDefinition CACIQUE = new PieceDefinition() {
        @Override
        public String getId() {
            return "cacique";
        }

        @Override
        public Piece create(Color color) {
            return new Piece(getId(), color == Color.WHITE ? 'C' : 'c', color, 6, Set.of(),
                    List.of(new SlidingMovement(Direction.ORTHOGONAL, 2)));
        }
    };

    private static List<String> ids(List<Piece> pieces) {
        return pieces.stream().map(Piece::getId).toList();
    }

    /** Knight and bishop are worth the same: ties keep the factory's registration order. */
    @Test
    void choices_excludeRoyalAndPromotingPieces_mostValuableFirst() {
        assertEquals(List.of("queen", "rook", "knight", "bishop"), ids(rule.getPromotionChoices(Color.WHITE)));
    }

    @Test
    void choices_haveTheRequestedColor() {
        assertTrue(rule.getPromotionChoices(Color.BLACK).stream().allMatch(p -> p.getColor() == Color.BLACK));
    }

    @Test
    void registeringANewPiece_addsItToTheChoices() {
        pieceFactory.register(CACIQUE);

        assertEquals(List.of("queen", "cacique", "rook", "knight", "bishop"),
                ids(rule.getPromotionChoices(Color.WHITE)));
    }

    @Test
    void invalidOrMissingChoice_promotesToTheMostValuableChoice() {
        Board board = new Board(8, 8);
        board.placePiece(pieceFactory.create("pawn", Color.WHITE), new Position(6, 0));
        Move toKing = new Move(new Position(6, 0), new Position(7, 0), "king");

        MoveCommand command = rule.createCommand(board, toKing);
        command.execute(board);

        assertEquals("queen", board.getPiece(new Position(7, 0)).getId());
    }
}
