package ar.edu.uade.chess.adapter.gui;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.piece.Piece;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps the captured-pieces panel of the window: which pieces each side has taken and
 * the material advantage. It compares the board before and after each move, so it works
 * for every kind of capture (normal, en passant, with promotion). No Swing here, so it
 * can be tested on its own.
 */
final class CapturedPiecesTracker {
    private static final Comparator<Piece> DISPLAY_ORDER =
            Comparator.comparingInt(Piece::getValue).reversed().thenComparing(Piece::getSymbol);

    private final Map<Color, List<Piece>> captures = new EnumMap<>(Color.class);
    private final Deque<Snapshot> history = new ArrayDeque<>();
    private Board boardBeforeLastMove;

    CapturedPiecesTracker(Board initialBoard) {
        reset(initialBoard);
    }

    void reset(Board initialBoard) {
        for (Color color : Color.values()) {
            captures.put(color, new ArrayList<>());
        }
        history.clear();
        boardBeforeLastMove = initialBoard;
    }

    /** Registers a move just played by the capturer; afterMove is the board once it was executed. */
    void recordMove(Board afterMove, Color capturer) {
        history.push(new Snapshot(copyOfCaptures(), boardBeforeLastMove));
        List<Piece> taken = captures.get(capturer);
        taken.addAll(piecesLost(boardBeforeLastMove, afterMove, capturer.opposite()));
        taken.sort(DISPLAY_ORDER);
        boardBeforeLastMove = afterMove;
    }

    /** Goes back to the state before the last recorded move. False if there is nothing to undo. */
    boolean undoLastMove() {
        Snapshot previous = history.poll();
        if (previous == null) {
            return false;
        }
        captures.putAll(previous.captures());
        boardBeforeLastMove = previous.boardBefore();
        return true;
    }

    /** Pieces taken by the given side, most valuable first. */
    List<Piece> capturedBy(Color capturer) {
        return List.copyOf(captures.get(capturer));
    }

    /** Material the given side is ahead by (0 if it is not ahead). */
    int advantageOf(Color color) {
        return Math.max(0, totalValue(color) - totalValue(color.opposite()));
    }

    private int totalValue(Color capturer) {
        return captures.get(capturer).stream().mapToInt(Piece::getValue).sum();
    }

    /** Pieces of victimColor present before the move and missing after it. */
    private List<Piece> piecesLost(Board before, Board after, Color victimColor) {
        Map<String, Integer> remaining = countById(after, victimColor);
        List<Piece> lost = new ArrayList<>();
        for (Position position : before.getPositionsOf(victimColor)) {
            Piece piece = before.getPiece(position);
            int left = remaining.getOrDefault(piece.getId(), 0);
            if (left > 0) {
                remaining.put(piece.getId(), left - 1);
            } else {
                lost.add(piece.copy());
            }
        }
        return lost;
    }

    private Map<String, Integer> countById(Board board, Color color) {
        Map<String, Integer> counts = new HashMap<>();
        for (Position position : board.getPositionsOf(color)) {
            counts.merge(board.getPiece(position).getId(), 1, Integer::sum);
        }
        return counts;
    }

    private Map<Color, List<Piece>> copyOfCaptures() {
        Map<Color, List<Piece>> copy = new EnumMap<>(Color.class);
        captures.forEach((color, pieces) -> copy.put(color, new ArrayList<>(pieces)));
        return copy;
    }

    private record Snapshot(Map<Color, List<Piece>> captures, Board boardBefore) { }
}
