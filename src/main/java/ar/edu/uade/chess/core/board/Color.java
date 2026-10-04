package ar.edu.uade.chess.core.board;

public enum Color {
    WHITE(1),
    BLACK(-1);

    private final int forward;

    Color(int forward) {
        this.forward = forward;
    }

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    /** Row direction in which this color advances: +1 for white, -1 for black. */
    public int forward() {
        return forward;
    }
}
