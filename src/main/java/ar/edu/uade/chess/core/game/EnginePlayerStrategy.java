package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.in.GameQueries;
import ar.edu.uade.chess.core.port.out.ChessEngine;

import java.util.List;

/**
 * Computer player backed by a chess engine. The engine proposes moves, only the legal ones
 * are kept, and the level picks one. When the engine has nothing to offer (it is missing
 * or failed) the fallback player moves instead, so the game never stops.
 */
public class EnginePlayerStrategy implements PlayerStrategy {
    private final ChessEngine engine;
    private final EngineLevel level;
    private final PlayerStrategy fallback;

    public EnginePlayerStrategy(ChessEngine engine, EngineLevel level, PlayerStrategy fallback) {
        this.engine = engine;
        this.level = level;
        this.fallback = fallback;
    }

    @Override
    public boolean isAutomatic() {
        return true;
    }

    @Override
    public Move chooseMove(GameQueries game, Color color) {
        List<Move> legal = game.getLegalMoves();
        List<Move> candidates = engine.candidateMoves(game, level.difficulty()).stream()
                .filter(legal::contains)
                .toList();
        if (candidates.isEmpty()) {
            return fallback.chooseMove(game, color);
        }
        return level.choose(candidates, game);
    }
}
