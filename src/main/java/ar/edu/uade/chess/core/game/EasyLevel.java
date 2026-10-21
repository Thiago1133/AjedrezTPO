package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.in.GameQueries;

import java.util.List;
import java.util.Random;

/**
 * Easy (below 800 Elo): captures whenever the rules allow any capture, even one the engine
 * would not suggest. Otherwise, most of the time a random legal move, and now and then one of
 * the engine's candidates, also at random.
 */
public class EasyLevel implements EngineLevel {
    private final Random random;
    private final double randomMoveShare;

    /** @param randomMoveShare from 0 (always an engine candidate) to 1 (always any legal move) */
    public EasyLevel(Random random, double randomMoveShare) {
        this.random = random;
        this.randomMoveShare = randomMoveShare;
    }

    @Override
    public Difficulty difficulty() {
        return Difficulty.EASY;
    }

    @Override
    public Move choose(List<Move> candidates, GameQueries game) {
        List<Move> captures = game.getLegalMoves().stream().filter(game::isCapture).toList();
        if (!captures.isEmpty()) return pickAtRandom(captures);
        return random.nextDouble() < randomMoveShare ? pickAtRandom(game.getLegalMoves()) : pickAtRandom(candidates);
    }

    private Move pickAtRandom(List<Move> moves) {
        return moves.get(random.nextInt(moves.size()));
    }
}
