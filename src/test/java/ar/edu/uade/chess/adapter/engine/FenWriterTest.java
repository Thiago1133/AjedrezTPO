package ar.edu.uade.chess.adapter.engine;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.game.FullGameWiring;
import ar.edu.uade.chess.core.game.Game;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.StandardChessSetup;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** FEN of real game positions, written in memory (no engine is started). */
class FenWriterTest {
    private final PieceFactory pieceFactory = FullGameWiring.standardPieces();

    private static Move move(String from, String to) {
        return new Move(new Position(from.charAt(1) - '1', from.charAt(0) - 'a'),
                new Position(to.charAt(1) - '1', to.charAt(0) - 'a'));
    }

    private Game standardGame() {
        Board board = new Board(8, 8);
        new StandardChessSetup(pieceFactory).setup(board);
        Game game = FullGameWiring.twoPlayers(board, pieceFactory);
        game.start();
        return game;
    }

    @Test
    void startingPosition() {
        assertEquals(Optional.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"),
                FenWriter.write(standardGame()));
    }

    @Test
    void afterADoubleStep_blackToMove_withTheEnPassantSquare() {
        Game game = standardGame();
        game.move(move("e2", "e4"));

        assertEquals(Optional.of("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"),
                FenWriter.write(game));
    }

    @Test
    void movingARook_losesCastlingOnThatSide() {
        Game game = standardGame();
        assertTrue(game.move(move("g1", "f3")));
        assertTrue(game.move(move("a7", "a5")));
        assertTrue(game.move(move("h1", "g1")));
        assertTrue(game.move(move("a8", "a6")));

        String fen = FenWriter.write(game).orElseThrow();

        assertTrue(fen.endsWith(" w Qk - 0 1"), "white keeps the queen side, black the king side: " + fen);
    }

    @Test
    void movingTheKing_losesBothCastlingRights() {
        Game game = standardGame();
        assertTrue(game.move(move("e2", "e3")));
        assertTrue(game.move(move("e7", "e6")));
        assertTrue(game.move(move("e1", "e2")));

        String fen = FenWriter.write(game).orElseThrow();

        assertTrue(fen.endsWith(" b kq - 0 1"), "only black can still castle: " + fen);
    }

    @Test
    void aPositionThatIsNotEightByEight_hasNoFen() {
        Board board = new Board(8, 10);
        new StandardChessSetup(pieceFactory).setup(board);
        Game game = FullGameWiring.twoPlayers(board, pieceFactory);

        assertEquals(Optional.empty(), FenWriter.write(game));
    }
}
