package ar.edu.uade.chess.adapter;

import ar.edu.uade.chess.core.game.Difficulty;
import ar.edu.uade.chess.core.game.TimeControl;

/** What the player chose for a new game, in the window or in the console. */
public record GameSettings(boolean versusComputer, TimeControl timeControl, Difficulty difficulty) {
}
