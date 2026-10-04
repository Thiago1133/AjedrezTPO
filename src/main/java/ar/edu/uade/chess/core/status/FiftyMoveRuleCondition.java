package ar.edu.uade.chess.core.status;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.command.MoveCommand;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.game.GameStatus;

import java.util.List;

/** Draw after 50 moves by each side (100 half-moves) with no capture or pawn move. */
public class FiftyMoveRuleCondition implements GameEndCondition {
    private static final int HALF_MOVE_LIMIT = 100;

    @Override
    public boolean isMet(Board board, MoveHistory history, Color toMove) {
        List<MoveCommand> executed = history.getExecuted();
        int reversibleHalfMoves = 0;
        for (int i = executed.size() - 1; i >= 0 && !executed.get(i).isIrreversible(); i--) {
            reversibleHalfMoves++;
        }
        return reversibleHalfMoves >= HALF_MOVE_LIMIT;
    }

    @Override
    public GameStatus getResult() {
        return GameStatus.DRAW;
    }
}
