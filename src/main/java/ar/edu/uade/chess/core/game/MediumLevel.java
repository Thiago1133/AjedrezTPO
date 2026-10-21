package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.in.GameQueries;

import java.util.List;
import java.util.Random;

/**
 * Medium (about 800 Elo): gives check whenever one of the candidates does. Otherwise it plays
 * the engine's choice, except for a share of its moves that are picked at random among the
 * legal ones, which makes it miss things as a weaker player does.
 */
public class MediumLevel implements EngineLevel {
    private final Random random;
    private final double randomMoveShare;

    /** @param randomMoveShare from 0 (never a random move) to 1 (always, when it cannot check) */
    public MediumLevel(Random random, double randomMoveShare) {
        this.random = random;
        this.randomMoveShare = randomMoveShare;
    }

    @Override
    public Difficulty difficulty() {
        return Difficulty.MEDIUM;
    }

    @Override
    public Move choose(List<Move> candidates, GameQueries game) {
        return candidates.stream().filter(game::givesCheck).findFirst()
                .orElseGet(() -> random.nextDouble() < randomMoveShare
                        ? pickAtRandom(game.getLegalMoves())
                        : candidates.get(0));
    }

    private Move pickAtRandom(List<Move> moves) {
        return moves.get(random.nextInt(moves.size()));
    }
}
