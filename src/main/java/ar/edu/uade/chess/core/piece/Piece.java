package ar.edu.uade.chess.core.piece;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A piece is defined by composition: its movement rules and its traits.
 * There are no subclasses per piece type.
 */
public class Piece {
    private final String id;
    private final char symbol;
    private final Color color;
    private final int value;
    private final Set<PieceTrait> traits;
    private final List<MovementRule> movementRules;
    private boolean moved;

    public Piece(String id, char symbol, Color color, int value,
                 Set<PieceTrait> traits, List<MovementRule> movementRules) {
        if (movementRules.isEmpty()) {
            throw new IllegalArgumentException("A piece needs at least one movement rule: " + id);
        }
        this.id = id;
        this.symbol = symbol;
        this.color = color;
        this.value = value;
        this.traits = Set.copyOf(traits);
        this.movementRules = List.copyOf(movementRules);
    }

    public String getId() {
        return id;
    }

    public char getSymbol() {
        return symbol;
    }

    public Color getColor() {
        return color;
    }

    public int getValue() {
        return value;
    }

    public boolean hasTrait(PieceTrait trait) {
        return traits.contains(trait);
    }

    public boolean hasMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    /** True if any of its movement rules allows the move (check is not considered). */
    public boolean canMove(Position from, Position to, Board board) {
        for (MovementRule rule : movementRules) {
            if (rule.canMove(this, from, to, board)) {
                return true;
            }
        }
        return false;
    }

    public List<Position> getReachablePositions(Position from, Board board) {
        Set<Position> result = new LinkedHashSet<>();
        for (MovementRule rule : movementRules) {
            result.addAll(rule.getReachablePositions(this, from, board));
        }
        return new ArrayList<>(result);
    }

    /** Independent copy; movement rules are stateless so they are shared. */
    public Piece copy() {
        Piece copy = new Piece(id, symbol, color, value, traits, movementRules);
        copy.setMoved(moved);
        return copy;
    }

    @Override
    public String toString() {
        return color + " " + id;
    }
}
