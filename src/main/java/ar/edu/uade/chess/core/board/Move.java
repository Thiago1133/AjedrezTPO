package ar.edu.uade.chess.core.board;

import java.util.Objects;

/** A move request: origin, destination and, optionally, the piece id to promote to. */
public final class Move {
    private final Position from;
    private final Position to;
    private final String promotionId;

    public Move(Position from, Position to) {
        this(from, to, null);
    }

    public Move(Position from, Position to, String promotionId) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        this.promotionId = promotionId;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    /** Piece id chosen for promotion, or null when none was requested. */
    public String getPromotionId() {
        return promotionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move other)) return false;
        return from.equals(other.from) && to.equals(other.to) && Objects.equals(promotionId, other.promotionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, promotionId);
    }

    @Override
    public String toString() {
        return from + " -> " + to + (promotionId == null ? "" : " =" + promotionId);
    }
}
