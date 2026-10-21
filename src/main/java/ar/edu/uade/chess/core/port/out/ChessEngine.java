package ar.edu.uade.chess.core.port.out;

import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.game.Difficulty;
import ar.edu.uade.chess.core.port.in.GameQueries;

import java.util.List;

/**
 * Driven port: an outside chess engine that proposes moves. The core decides which of
 * them is played and never knows which engine answers or how.
 */
public interface ChessEngine {
    /** Candidate moves for the player to move, best first; empty if the engine cannot answer. */
    List<Move> candidateMoves(GameQueries game, Difficulty difficulty);
}
