package ar.edu.uade.chess.core.status;

import ar.edu.uade.chess.core.board.Board;
import ar.edu.uade.chess.core.board.Color;
import ar.edu.uade.chess.core.command.MoveHistory;
import ar.edu.uade.chess.core.game.GameStatus;
import ar.edu.uade.chess.core.rules.CheckDetector;

import java.util.List;

/** Decides the game status: the first end condition met wins; otherwise check or in progress. */
public class GameStatusEvaluator {
    private final CheckDetector checkDetector;
    private final List<GameEndCondition> endConditions;

    public GameStatusEvaluator(CheckDetector checkDetector, List<GameEndCondition> endConditions) {
        this.checkDetector = checkDetector;
        this.endConditions = List.copyOf(endConditions);
    }

    public GameStatus evaluate(Board board, MoveHistory history, Color toMove) {
        for (GameEndCondition condition : endConditions) {
            if (condition.isMet(board, history, toMove)) {
                return condition.getResult();
            }
        }
        return checkDetector.isInCheck(board, toMove) ? GameStatus.CHECK : GameStatus.IN_PROGRESS;
    }
}
