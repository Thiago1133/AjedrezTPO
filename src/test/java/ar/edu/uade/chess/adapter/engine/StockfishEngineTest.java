package ar.edu.uade.chess.adapter.engine;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.game.Difficulty;
import ar.edu.uade.chess.core.game.FullGameWiring;
import ar.edu.uade.chess.core.game.Game;
import ar.edu.uade.chess.core.piece.PieceFactory;
import ar.edu.uade.chess.core.piece.StandardChessSetup;
import ar.edu.uade.chess.core.rules.PromotionRule;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Without the engine's executable no process is started and no move is proposed. */
class StockfishEngineTest {

    @Test
    void withoutTheExecutable_itIsUnavailable_andProposesNothing() {
        PieceFactory pieceFactory = FullGameWiring.standardPieces();
        Board board = new Board(8, 8);
        new StandardChessSetup(pieceFactory).setup(board);
        Game game = FullGameWiring.twoPlayers(board, pieceFactory);
        try (StockfishEngine engine = new StockfishEngine(Path.of("missing", "stockfish.exe"), new PromotionRule(pieceFactory))) {
            assertFalse(engine.isAvailable());
            assertEquals(List.of(), engine.candidateMoves(game, Difficulty.HARD));
        }
    }
}
