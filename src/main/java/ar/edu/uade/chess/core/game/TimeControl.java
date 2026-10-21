package ar.edu.uade.chess.core.game;

/** Available starting times for a game. How each one is named on screen is up to the adapters. */
public enum TimeControl {
    THIRTY_MINUTES(30 * 60),
    TEN_MINUTES(10 * 60),
    FIVE_MINUTES(5 * 60),
    UNLIMITED(-1);

    private final int initialSeconds;

    TimeControl(int initialSeconds) {
        this.initialSeconds = initialSeconds;
    }

    public int getInitialSeconds() {
        return initialSeconds;
    }

    public boolean isUnlimited() {
        return this == UNLIMITED;
    }
}
