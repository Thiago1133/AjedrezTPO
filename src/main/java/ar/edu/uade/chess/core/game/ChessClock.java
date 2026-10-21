package ar.edu.uade.chess.core.game;

import ar.edu.uade.chess.core.board.Color;

import java.util.EnumMap;
import java.util.Map;

/**
 * Each side's remaining time. It starts after White's first move (White thinks its first
 * move for free) and only runs for the side to move. The real passing of time comes from
 * outside (an adapter's timer calls Game.advanceClock); this class only keeps the account.
 */
public class ChessClock {
    private final TimeControl timeControl;
    private final Map<Color, Integer> remainingSeconds = new EnumMap<>(Color.class);
    private boolean started;

    public ChessClock(TimeControl timeControl) {
        this.timeControl = timeControl;
        for (Color color : Color.values()) {
            remainingSeconds.put(color, timeControl.getInitialSeconds());
        }
    }

    public TimeControl getTimeControl() {
        return timeControl;
    }

    /** Called after every move; White's first move starts the clock. */
    public void moveMade(Color mover) {
        if (!timeControl.isUnlimited() && mover == Color.WHITE) {
            started = true;
        }
    }

    /**
     * Takes the seconds from the side to move. True if that side has just run out of time;
     * nothing happens while the clock has not started or has no limit.
     */
    public boolean tick(Color toMove, int seconds) {
        if (seconds <= 0 || !started || timeControl.isUnlimited()) {
            return false;
        }
        int left = Math.max(0, remainingSeconds.get(toMove) - seconds);
        remainingSeconds.put(toMove, left);
        return left == 0;
    }

    /** Remaining seconds, or a negative value when there is no limit. */
    public int getRemainingSeconds(Color color) {
        return remainingSeconds.get(color);
    }
}
