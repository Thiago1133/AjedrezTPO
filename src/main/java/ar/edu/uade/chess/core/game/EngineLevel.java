package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Move;
import ar.edu.uade.chess.core.port.in.GameQueries;

import java.util.List;

/** How the computer picks one of the engine's candidate moves at a difficulty (Strategy). */
public interface EngineLevel {
    Difficulty difficulty();

    /** One of the candidates, which are legal, best first and never empty. */
    Move choose(List<Move> candidates, GameQueries game);
}
