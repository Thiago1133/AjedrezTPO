package ar.edu.uade.chess.core.game;

/** Available starting times for a game. */
public enum TimeControl {
    THIRTY_MINUTES(30 * 60, "30 minutos"),
    TEN_MINUTES(10 * 60, "10 minutos"),
    FIVE_MINUTES(5 * 60, "5 minutos"),
    UNLIMITED(-1, "Ilimitado");

    private final int initialSeconds;
    private final String displayName;

    TimeControl(int initialSeconds, String displayName) {
        this.initialSeconds = initialSeconds;
        this.displayName = displayName;
    }

    public int getInitialSeconds() {
        return initialSeconds;
    }

    public boolean isUnlimited() {
        return this == UNLIMITED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
