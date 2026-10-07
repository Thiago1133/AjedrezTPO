package ar.edu.uade.chess.core.status;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.board.Position;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.game.GameStatus;
import ar.edu.uade.chess.core.piece.Piece;

import java.util.List;

/**
 * Draw when the same position (same pieces on the same squares, same player to
 * move) appears for the third time. Past positions are rebuilt by undoing moves
 * on a copy of the board, only back to the last irreversible move: no earlier
 * position can repeat. Simplification: castling and en passant rights are not
 * part of the comparison.
 */
public class ThreefoldRepetitionCondition implements GameEndCondition {
    private static final int REPETITIONS = 3;

    @Override
    public boolean isMet(Board board, MoveHistory history, Color toMove) {
        Board past = board.copy();
        Color side = toMove;
        int occurrences = 1;
        List<MoveCommand> executed = history.getExecuted();
        for (int i = executed.size() - 1; i >= 0 && !executed.get(i).isIrreversible(); i--) {
            executed.get(i).undo(past);
            side = side.opposite();
            if (side == toMove && samePlacement(board, past)) {
                occurrences++;
            }
        }
        return occurrences >= REPETITIONS;
    }

    @Override
    public GameStatus getResult() {
        return GameStatus.DRAW;
    }

    /** Same kind and color of piece on every square (compares symbols and colors, never text). */
    private static boolean samePlacement(Board current, Board past) {
        for (int row = 0; row < current.getRows(); row++) {
            for (int column = 0; column < current.getColumns(); column++) {
                Position position = new Position(row, column);
                if (!sameKind(current.getPiece(position), past.getPiece(position))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean sameKind(Piece a, Piece b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.getSymbol() == b.getSymbol() && a.getColor() == b.getColor();
    }
}
