package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.in.GameQueries;

import java.util.List;

/**
 * Hard: the engine's best move at full strength. It looks far enough ahead to look for
 * checks and to keep its own king covered against threats.
 */
public class HardLevel implements EngineLevel {
    @Override
    public Difficulty difficulty() {
        return Difficulty.HARD;
    }

    @Override
    public Move choose(List<Move> candidates, GameQueries game) {
        return candidates.get(0);
    }
}
