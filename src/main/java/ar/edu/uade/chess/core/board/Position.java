package ar.edu.uade.chess.core.board;

import java.util.Objects;

/** Immutable board coordinate. Row 0 is white's back rank, column 0 is file "a". */
public final class Position {
    private final int row;
    private final int column;

    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Position offset(Direction direction) {
        return new Position(row + direction.getRowStep(), column + direction.getColumnStep());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position other)) return false;
        return row == other.row && column == other.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + column + ")";
    }
}
