package ar.edu.uade.chess.core.board;

import java.util.List;
import java.util.Objects;

/** Immutable step (row delta, column delta) used to describe how pieces move. */
public final class Direction {
    public static final List<Direction> ORTHOGONAL = List.of(
            new Direction(1, 0), new Direction(-1, 0), new Direction(0, 1), new Direction(0, -1));
    public static final List<Direction> DIAGONAL = List.of(
            new Direction(1, 1), new Direction(1, -1), new Direction(-1, 1), new Direction(-1, -1));
    public static final List<Direction> ALL = List.of(
            new Direction(1, 0), new Direction(-1, 0), new Direction(0, 1), new Direction(0, -1),
            new Direction(1, 1), new Direction(1, -1), new Direction(-1, 1), new Direction(-1, -1));

    private final int rowStep;
    private final int columnStep;

    public Direction(int rowStep, int columnStep) {
        this.rowStep = rowStep;
        this.columnStep = columnStep;
    }

    public int getRowStep() {
        return rowStep;
    }

    public int getColumnStep() {
        return columnStep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Direction other)) return false;
        return rowStep == other.rowStep && columnStep == other.columnStep;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowStep, columnStep);
    }

    @Override
    public String toString() {
        return "Direction(" + rowStep + ", " + columnStep + ")";
    }
}
