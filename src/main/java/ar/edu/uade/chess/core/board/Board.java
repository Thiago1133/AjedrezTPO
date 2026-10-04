package ar.edu.uade.chess.core.board;

import ar.edu.uade.chess.core.piece.Piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rectangular board of any size. It only stores pieces and answers geometric
 * questions; it knows nothing about chess rules.
 */
public class Board {
    private final int rows;
    private final int columns;
    private final Map<Position, Piece> pieces = new HashMap<>();

    public Board(int rows, int columns) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("Board dimensions must be positive: " + rows + "x" + columns);
        }
        this.rows = rows;
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    /** Returns the piece at the position, or null if the square is empty. */
    public Piece getPiece(Position position) {
        return pieces.get(position);
    }

    public boolean isEmpty(Position position) {
        return !pieces.containsKey(position);
    }

    public boolean isInside(Position position) {
        return position.getRow() >= 0 && position.getRow() < rows
                && position.getColumn() >= 0 && position.getColumn() < columns;
    }

    /**
     * True when every square strictly between from and to is empty.
     * Only meaningful for straight or diagonal lines; returns false otherwise.
     */
    public boolean isPathClear(Position from, Position to) {
        int rowDelta = to.getRow() - from.getRow();
        int columnDelta = to.getColumn() - from.getColumn();
        boolean straight = rowDelta == 0 || columnDelta == 0;
        boolean diagonal = Math.abs(rowDelta) == Math.abs(columnDelta);
        if (from.equals(to) || !(straight || diagonal)) {
            return false;
        }
        Direction step = new Direction(Integer.signum(rowDelta), Integer.signum(columnDelta));
        for (Position current = from.offset(step); !current.equals(to); current = current.offset(step)) {
            if (!isEmpty(current)) {
                return false;
            }
        }
        return true;
    }

    public void placePiece(Piece piece, Position position) {
        requireInside(position);
        pieces.put(position, piece);
    }

    /** Moves the piece at from to to. Whatever was at to is overwritten. */
    public void movePiece(Position from, Position to) {
        requireInside(to);
        Piece piece = pieces.remove(from);
        if (piece == null) {
            throw new IllegalStateException("No piece at " + from);
        }
        pieces.put(to, piece);
    }

    /** Removes and returns the piece at the position, or null if it was empty. */
    public Piece removePiece(Position position) {
        return pieces.remove(position);
    }

    public List<Position> getPositionsOf(Color color) {
        List<Position> result = new ArrayList<>();
        for (Map.Entry<Position, Piece> entry : pieces.entrySet()) {
            if (entry.getValue().getColor() == color) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /** Deep copy: pieces are copied too, so the copy can be mutated freely. */
    public Board copy() {
        Board copy = new Board(rows, columns);
        for (Map.Entry<Position, Piece> entry : pieces.entrySet()) {
            copy.pieces.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }

    private void requireInside(Position position) {
        if (!isInside(position)) {
            throw new IllegalArgumentException("Position outside the board: " + position);
        }
    }
}
